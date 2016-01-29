/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import net.yatomiya.e4.util.*;

public class StyleNodeBuilder {
    private TemplateManager templateManager;
    private Map<String, StyleTag> tagRegistry;
    private Map<String, StyleAttribute> attrRegistry;

    /**
     * なぜ StyleNode.getData() を使わないのか
     * StyleNode.getData() で上位ノードのデータを使う場合、上位の StyleNode に add されていなければならない。
     * しかし、StyleNode をつなげた状態で add していくと、 add したときに StyleNode 全体を走査する処理が
     * 走ってしまう場合があり（ブロック整形 formatBlock())、パフォーマンス的に好ましくない。
     * なので、build() で生成するノードを全て作成した後に、ノードを親ノードに取り付ける順番を取っている。
     * しかしそうすると、取り付けるまでは親ノードより上位の getData() を参照することができない。
     * なので、ビルダー内部にコンテキストデータ用の仕組みを準備する。
     */
    private Map<Object, Object> dataMap;
    private Deque<Map<Object, Object>> dataStack;

    public StyleNodeBuilder(TemplateManager templateManager) {
        this.templateManager = templateManager;
        tagRegistry = new HashMap<>();
        attrRegistry = new HashMap<>();

        for (StyleTag tag : StyleTag.getStandardTags()) {
            addStyleTag(tag.getName(), tag);
        }
        for (StyleAttribute attr : StyleAttribute.getStandardAttributes()) {
            addStyleAttribute(attr.getName(), attr);
        }

        dataMap = new HashMap<>();
        dataStack = new ArrayDeque<>();
    }

    public void addStyleTag(String name, StyleTag tag) {
        tagRegistry.put(name, tag);
    }

    public void addStyleTag(StyleTag tag) {
        addStyleTag(tag.getName(), tag);
    }

    public StyleTag getStyleTag(String name) {
        StyleTag tag = tagRegistry.get(name);
        if (tag == null) {
            //JUtils.println("Unsupported tag: " + name);

            // Register new plain ElementStyleTag with unknown name.
            tag = new ElementStyleTag(name, false);
            addStyleTag(name, tag);
        }
        return tag;
    }

    public StyleTag getStyleTag(Node node) {
        String name = null;
        if (node instanceof Element)
            name = ((Element)node).tagName();
        else if (node instanceof TextNode)
            name = StyleTag.INTERNAL_HTML_TEXT.getName();
        else if (node instanceof Comment)
            name = StyleTag.INTERNAL_HTML_COMMENT.getName();

        return getStyleTag(name);
    }

    public void addStyleAttribute(String name, StyleAttribute attr) {
        attrRegistry.put(name, attr);
    }

    public StyleAttribute getStyleAttribute(String name) {
        StyleAttribute attr = attrRegistry.get(name);

        if (attr == null) {
            attr = new StyleAttribute(name, false, false);
            attrRegistry.put(name, attr);
        }

        return attr;
    }

    public Object getData(Object key) {
        return dataMap.get(key);
    }

    public <T> T getData(Class<T> clazz) {
        return (T)dataMap.get(clazz);
    }

    public void setData(Object key, Object value) {
        dataMap.put(key, value);
    }

    public void pushDataMap() {
        dataStack.push(dataMap);
        dataMap = new HashMap<>(dataMap);
    }

    public void popDataMap() {
        dataMap = dataStack.pop();
    }

    public StyleNode build(String html) {
        Element e = Jsoup.parse(html).body();

        return build(e);
    }

    public StyleNode build(Node node) {
        StyleTag tag = getStyleTag(node);
        StyleNode styleNode = tag.createStyleNode();
        if (node instanceof Element) {
            parseAttributeAll(styleNode, (Element)node);
        }
        tag.parse(node, styleNode, this);
        return styleNode;
    }

    public List<StyleNode> build(List<Node> nodes) {
        List<StyleNode> list = new ArrayList<>();
        for (Node node : nodes) {
            StyleNode styleNode = build(node);
            list.add(styleNode);
        }
        return list;
    }

    public StyleNode build(StyleTag tag) {
        StyleNode styleNode = tag.createStyleNode();
        parseAttributeAll(styleNode, null);
        return styleNode;
    }

    public void parseAttributeAll(StyleNode styleNode, Element node) {
        Attributes attrs = new Attributes();
        if (node != null) {
            attrs.addAll(node.attributes());
        }

        Attributes styleTemplate = getTemplateManager().getAttributeTemplate(styleNode.getTag().getName());
        if (styleTemplate != null) {
            attrs.addAll(styleTemplate);
        }

        for (Attribute attr : attrs) {
            String name = attr.getKey();
            if (!JUtils.isEmpty(name) && !JUtils.isEmpty(attr.getValue())) {
                StyleAttribute sa = getStyleAttribute(name);
                if (sa != null) {
                    styleNode.setAttribute(sa, attr.getValue());
                }
            }
        }
    }

    private static final String REENTRANT_PREFIX = StyleNodeBuilder.class.getName() + ":reentrant_prefix";

    public StyleNode buildTemplate(String templateId) {
        String reentrant_key = REENTRANT_PREFIX + templateId;
        if (getData(reentrant_key) != null)
            return null;

        StyleNode styleNode;
        Element template = getTemplateManager().getTemplate(templateId);

        pushDataMap();
        {
            setData(reentrant_key, Boolean.TRUE);

            if (template != null) {
                styleNode = build(template);
            } else {
                StyleNode node = StyleTag.TEXT.createStyleNode();
                node.setText(String.format("Template [%s] is not found.", templateId));
                styleNode = node;
            }
        }
        popDataMap();

        return styleNode;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }
}


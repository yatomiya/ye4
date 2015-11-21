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
            name = StyleTag.TAG_TEXT.getName();
        else if (node instanceof Comment)
            name = StyleTag.TAG_INTERNAL_COMMENT.getName();

        return getStyleTag(name);
    }

    public void addStyleAttribute(String name, StyleAttribute attr) {
        attrRegistry.put(name, attr);
    }

    public StyleAttribute getStyleAttribute(String name) {
        StyleAttribute attr = attrRegistry.get(name);
/*
           // For now, unregistered attribute in HTML node template is not set to StyleNode.
        if (attr == null) {
            attr = new StyleAttribute(name, false, false);
            attrRegistry.put(name, attr);
        }
*/
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
        tag.parse(node, styleNode, this);
        if (node instanceof Element) {
            parseAttributeAll((Element)node, styleNode);
        }
        return styleNode;
    }

    public List<StyleNode> build(List<Node> nodes) {
        List<StyleNode> list = new ArrayList<>();
        for (Node node : nodes) {
            StyleNode styleNode = build(node);
            if (styleNode.getTag() == StyleTag.TAG_TEXT
                && JUtils.isEmpty(((TextStyleNode)styleNode).getText())) {
            } else {
                list.add(styleNode);
            }
        }
        return list;
    }

    public void parseAttributeAll(Element node, StyleNode styleNode) {
        Attributes attrList = null;
        Attributes styleTemplate = getTemplateManager().getStyleTemplate(node.tagName());
        if (styleTemplate == null) {
            attrList = node.attributes();
        } else {
            attrList = node.attributes();
            attrList.addAll(styleTemplate);
        }

        for (Attribute attr : attrList) {
            String name = attr.getKey();
            if (JUtils.isNotEmpty(name) && JUtils.isNotEmpty(attr.getValue())) {
                StyleAttribute sa = getStyleAttribute(name);
                if (sa != null) {
                    styleNode.setAttribute(sa, attr.getValue());
                }
            }
        }
    }

    private static final String RIENTRANT_PREFIX = StyleNodeBuilder.class.getName() + ":rientrant_prefix";

    public StyleNode buildTemplate(String templateId) {
        String rientrant_key = RIENTRANT_PREFIX + templateId;
        if (getData(rientrant_key) != null)
            return null;

        StyleNode styleNode;
        Element template = getTemplateManager().getTemplate(templateId);

        pushDataMap();
        {
            setData(rientrant_key, Boolean.TRUE);

            if (template != null) {
                styleNode = build(template);
            } else {
                TextStyleNode node = StyleTag.TAG_TEXT.createStyleNode();
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


/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import java.util.*;
import org.eclipse.jface.text.*;
import groovy.lang.*;
import net.yatomiya.e4.ui.style.viewer.*;
import net.yatomiya.e4.util.*;

public class StyleNode implements TreeNode<StyleNode>, IRegion {
    private StyleTag tag;
    private StyleNode parent;
    private List<StyleNode> children;
    private boolean isEnable;
    private String text;
    private boolean cancelNextBlockNewline;

    private Map<Object, Object> dataMap;
    private Map<StyleAttribute, String> attrMap;
    private Map<StyleAttribute, Object> contextAttrMap;

    private int docRegionOffset;
    private int docRegionLength;

    public StyleNode(StyleTag tag) {
        this(tag, null);
    }

    public StyleNode(StyleTag tag, String text) {
        this.tag = tag;
        this.text = text;

        parent = null;
        children = Collections.EMPTY_LIST;
        isEnable = true;
        cancelNextBlockNewline = false;

        dataMap = Collections.EMPTY_MAP;
        attrMap = Collections.EMPTY_MAP;
        contextAttrMap = Collections.EMPTY_MAP;

        docRegionOffset = 0;
        docRegionLength = 0;
    }

    void setTag(StyleTag tag) {
        this.tag = tag;
    }

    public StyleTag getTag() {
        return tag;
    }

    @Override
    public int getOffset() {
        return docRegionOffset;
    }

    @Override
    public int getLength() {
        return docRegionLength;
    }

    public int getEnd() {
        int end = getOffset() + getLength() - 1;
        if (end < getOffset())
            end = getOffset();
        return end;
    }

    @Override
    public StyleNode getParent() {
        return parent;
    }

    @Override
    public List<StyleNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<StyleNode> getEnabledChildren() {
        return CUtils.filter(getChildren(), n -> n.isEnable());
    }

    public void addChild(StyleNode node) {
        addChild(getChildren().size(), node);
    }

    public void addChild(int index, StyleNode node) {
        List<StyleNode> list = new ArrayList<>();
        list.add(node);
        doReplaceChild(index, 0, list);
    }

    public void addChildAll(List<StyleNode> list) {
        addChildAll(getChildren().size(), list);
    }

    public void addChildAll(int index, List<StyleNode> list) {
        doReplaceChild(index, 0, list);
    }

    public void removeChild(StyleNode node) {
        removeChild(children.indexOf(node));
    }

    public void removeChild(int index) {
        doReplaceChild(index, 1, null);
    }

    public void removeChildrenAll() {
        doReplaceChild(0, getChildren().size(), null);
    }

    public void replaceChild(int index, StyleNode replaceNode) {
        List<StyleNode> list = new ArrayList<>();
        list.add(replaceNode);
        doReplaceChild(index, 1, list);
    }

    private void doReplaceChild(int index, int removeLength, List<StyleNode> replaceNodeList) {
        StyleDocument doc = getContextData(StyleDocument.class);
        IRegion replaceRegion = null;

        if (removeLength > 0) {
            StyleNode first = children.get(index);
            StyleNode last = children.get(index + removeLength - 1);
            replaceRegion = new Region(first.getOffset(), last.getOffset() + last.getLength() - first.getOffset());
        } else {
            int offset;
            if (children.size() == 0) {
                offset = getOffset();
            } else if (index == children.size()) {
                offset = getOffset() + getLength();
            } else {
                offset = children.get(index).getOffset();
            }
            replaceRegion = new Region(offset, 0);
        }

        // remove
        if (removeLength > 0) {
            for (int i = 0; i < removeLength; i++) {
                StyleNode n = children.get(index);
                children.remove(index);
                n.parent = null;
                n.evaluateContextAttributeAll();
            }

            if (children.size() == 0)
                children = Collections.EMPTY_LIST;
        }

        // add
        if (replaceNodeList != null) {
            if (children == Collections.EMPTY_LIST)
                children = new ArrayList<>();

            {
                // To avoid enormous replacing character in Document, which causes enormous document changed events,
                // we format block in child node before adding it to node in Document.
                StyleNode dummy = StyleTag.SPAN.createStyleNode();
                dummy.children = new ArrayList<>();
                dummy.children.addAll(replaceNodeList);
                dummy.formatBlock();
                dummy.children.clear();
            }

            children.addAll(index, replaceNodeList);

            for (StyleNode n : replaceNodeList) {
                n.parent = this;
                n.evaluateContextAttributeAll();
            }
        }

        if (doc != null) {
            StringBuilder sb = new StringBuilder();
            if (replaceNodeList != null) {
                for (StyleNode n : replaceNodeList) {
                    String s = n.buildText();
                    sb.append(s);
                }
            }

            String replaceText = sb.toString();
            doDocumentReplace(replaceRegion, replaceText);
        }
    }

    protected void doDocumentReplace(IRegion region, String replaceText) {
        StyleDocument doc = getContextData(StyleDocument.class);
        if (doc == null)
            return;

        if (replaceText == null)
            replaceText = "";

        StyleNode rootNode = getRoot();
        rootNode.calculateTextRegionTree();
        try {
            doc.replace(region.getOffset(), region.getLength(), replaceText);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        rootNode.formatBlock();
    }

    public void setText(String text) {
        if (JUtils.nullEquals(getText(), text))
            return;

        this.text = text;

        StyleDocument doc = getContextData(StyleDocument.class);
        if (doc != null) {
            doDocumentReplace(new Region(getOffset(), getLength()), text);
        }
    }

    public String getText() {
        return text;
    }

    void setCancelNextBlockNewline(boolean v) {
        cancelNextBlockNewline = v;
    }

    boolean isCancelNextBlockNewline() {
        return cancelNextBlockNewline;
    }

    public String buildText() {
        StringBuilder sb = new StringBuilder();

        visitNodeTree(node -> {
                if (!JUtils.isEmpty(node.getText())) {
                    sb.append(node.getText());
                }
                return true;
            });

        return sb.toString();
    }

    public void setAttribute(StyleAttribute attr, String value) {
        String oldValue = attrMap.get(attr);
        if (!JUtils.nullEquals(value, oldValue)) {
            if (value != null) {
                if (attrMap == Collections.EMPTY_MAP)
                    attrMap = new SmallMap<>();

                attrMap.put(attr, value);
            } else {
                attrMap.remove(attr);

                if (attrMap.size() == 0)
                    attrMap = Collections.EMPTY_MAP;
            }

            visitNodeTree(node -> {
                    node.evaluateContextAttribute(attr);
                    return true;
                });

            if (attr.isPresentational()) {
                StyleViewer viewer = getContextData(StyleViewer.class);
                if (viewer != null) {
                    viewer.invalidateTextPresentation(getOffset(), getLength());
                }
            }
        }
    }

    protected void evaluateContextAttributeAll() {
        visitNodeTree(node -> {
                if (node.contextAttrMap.size() > 0)
                    node.contextAttrMap.clear();

                Set<StyleAttribute> set = new HashSet();
                if (node.getParent() != null && node.getParent().contextAttrMap.size() > 0)
                    set.addAll(node.getParent().contextAttrMap.keySet());
                if (node.attrMap.size() > 0)
                    set.addAll(node.attrMap.keySet());

                for (StyleAttribute attr : set) {
                    node.evaluateContextAttribute(attr);
                }
                return true;
            });
    }

    protected void evaluateContextAttribute(StyleAttribute attr) {
        Object value = null;
        String str = attrMap.get(attr);
        if (!JUtils.isEmpty(str)) {
            value = attr.parseValue(str);
        }

        if (attr.isCascadable()) {
            Object cascadedValue = null;
            if (getParent() != null)
                cascadedValue = getParent().contextAttrMap.get(attr);

            if (cascadedValue == null) {
                // value is value.
            } else if (value == null) {
                value = cascadedValue;
            } else if (value == StyleAttribute.DEFAULT_VALUE) {
                value = null;
            } else {
                value = attr.evaluateContextValue(value, cascadedValue);
            }
        }

        if (value == null) {
            contextAttrMap.remove(attr);

            if (contextAttrMap.size() == 0)
                contextAttrMap = Collections.EMPTY_MAP;
        } else {
            if (contextAttrMap == Collections.EMPTY_MAP)
                contextAttrMap = new SmallMap<>();

            contextAttrMap.put(attr, value);
        }
    }

    public String getAttribute(StyleAttribute attr) {
        return attrMap.get(attr);
    }

    public String getAttribute(String name) {
        for (Map.Entry<StyleAttribute, String> entry : attrMap.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<StyleAttribute, String> getAttributeMap() {
        return Collections.unmodifiableMap(attrMap);
    }

    public Object getContextAttribute(StyleAttribute attr) {
        return contextAttrMap.get(attr);
    }

    public Map<StyleAttribute, Object> getContextAttributeMap() {
        return Collections.unmodifiableMap(contextAttrMap);
    }

    public Object getData(Object key) {
        return dataMap.get(key);
    }

    public <T> T getData(Class<T> clazz) {
        return (T)getData((Object)clazz);
    }

    public Map<Object, Object> getDataMap() {
        return Collections.unmodifiableMap(dataMap);
    }

    public Object getContextData(Object key) {
        StyleNode node = findAncestor(n -> n.getData(key) != null);
        if (node != null)
            return node.getData(key);
        return null;
    }

    public <T> T getContextData(Class<T> clazz) {
        return (T)getContextData((Object)clazz);
    }

    public void setData(Object key, Object value) {
        if (value != null) {
            if (dataMap == Collections.EMPTY_MAP)
                dataMap = new SmallMap<>();
            dataMap.put(key, value);
        } else {
            if (dataMap != null) {
                dataMap.remove(key);
                if (dataMap.size() == 0)
                    dataMap = Collections.EMPTY_MAP;
            }
        }
    }

    public boolean isEnable() {
        return isEnable;
    }

    public boolean isContextEnable() {
        return findAncestor(n -> !n.isEnable()) == null;
    }

    public void setEnable(boolean v) {
        if (isEnable != v) {
            boolean oldEnable = isEnable;
            boolean oldContextEnable = isContextEnable();

            isEnable = v;
            boolean isContextEnable = isContextEnable();

            evaluateContextAttributeAll();

            if (oldContextEnable != isContextEnable) {
                StyleDocument doc = getContextData(StyleDocument.class);

                if (doc != null) {
                    if (isContextEnable) {
                        StyleNode aboveNode = findUpward(n -> (n != this) && n.isContextEnable() && getText() != null);

                        int offset = 0;
                        if (aboveNode != null) {
                            offset = aboveNode.getOffset() + aboveNode.getLength();
                        }

                        String nodeText = buildText();
                        doDocumentReplace(new Region(offset, 0), nodeText);
                    } else {
                        doDocumentReplace(new Region(getOffset(), getLength()), "");
                    }
                }
            }
        }
    }

    protected void runScriptAttribute(StyleAttribute attr, TemplateManager templateManager) {
        String scriptString = getAttribute(attr);
        if (JUtils.isEmpty(scriptString))
            return;
        Script script = templateManager.getScript(scriptString);
        if (script != null) {
            script.setProperty("node", this);
            script.run();
        }
    }

    public void update(TemplateManager templateManager) {
        runScriptAttribute(StyleAttribute.ONUPDATE, templateManager);
    }

    public void updateTree(TemplateManager templateManager) {
        visitTree(node -> {
                node.update(templateManager);
                return true;
            });
    }

    public static interface StyleNodeVisitor {
        // if returns false, child nodes is skipped.
        boolean visit(StyleNode node);

        // if returns false, visiting tree is forced to stop.
        default boolean visitTail(StyleNode node) {
            return true;
        }
    }

    public boolean visitNodeTree(StyleNodeVisitor visitor) {
        if (!isEnable())
            return true;

        boolean result = true;
        result = visitor.visit(this);

        if (result) {
            for (StyleNode child : new ArrayList<>(getChildren())) {
                result = child.visitNodeTree(visitor);
                if (!result)
                    break;
            }
        }

        result = visitor.visitTail(this);

        return result;
    }

    private void calculateTextRegionTree() {
        visitNodeTree(new StyleNodeVisitor() {
                int offset = 0;

                @Override
                public boolean visit(StyleNode node) {
                    // save region starting offset
                    node.docRegionOffset = offset;
                    node.docRegionLength = 0;

                    if (node.getText() != null) {
                        offset += node.getText().length();
                    }
                    return true;
                }

                @Override
                public boolean visitTail(StyleNode node) {
                    int startOffset = node.getOffset();
                    node.docRegionOffset = startOffset;
                    node.docRegionLength = offset - startOffset;

                    return true;
                }
            });
    }

    boolean isReentrantFormatBlock = false;

    public void formatBlock() {
        class Formatter {
            StyleNode lastTextNode;
            char lastChar = '\n';
            char lastLastChar = '\n';

            void format(StyleNode node) {
                if (!node.isEnable())
                    return;

                boolean headNeeded = false;
                boolean tailNeeded = false;

                List<StyleNode> nodeChildren = node.getEnabledChildren();

                if (node.getTag().isBlock()) {
                    if (lastChar == '\n') {
                        if (nodeChildren.size() > 0
                            && nodeChildren.get(0).getTag() == StyleTag.INTERNAL_NEWLINE_FOR_BLOCK) {
                            node.removeChild(0);
                        }
                    } else {
                        if (lastTextNode == null || !lastTextNode.isCancelNextBlockNewline()) {
                            lastLastChar = lastChar;
                            lastChar = '\n';

                            if (nodeChildren.size() == 0
                                || nodeChildren.get(0).getTag() != StyleTag.INTERNAL_NEWLINE_FOR_BLOCK) {
                                node.addChild(0, StyleTag.INTERNAL_NEWLINE_FOR_BLOCK.createStyleNode());
                            }
                        }
                    }
                } else {
                    String s = node.getText();
                    if (!JUtils.isEmpty(s)) {
                        lastTextNode = node;
                        lastLastChar = lastChar;
                        lastChar = s.charAt(s.length() - 1);
                    }
                }

                for (StyleNode child : new ArrayList<>(node.getChildren())) {
                    format(child);
                }

                if (node.getTag().isBlock()) {
                    if (lastChar == '\n') {
                        if (lastLastChar == '\n') {
                            if (nodeChildren.size() > 0
                                && nodeChildren.get(nodeChildren.size() - 1).getTag() == StyleTag.INTERNAL_NEWLINE_FOR_BLOCK) {
                                node.removeChild(node.getChildren().size() - 1);
                            }
                        }
                    } else {
                        lastLastChar = lastChar;
                        lastChar = '\n';

                        node.addChild(StyleTag.INTERNAL_NEWLINE_FOR_BLOCK.createStyleNode());
                    }
                }
            }
        }
        if (isReentrantFormatBlock)
            return;
        isReentrantFormatBlock = true;

        new Formatter().format(this);

        isReentrantFormatBlock = false;
    }

    public String toReadableString() {
        return toReadableString(false);
    }

    public String toReadableString(boolean showCascadedAttribute) {
        StringBuilder sb = new StringBuilder();
        visitNodeTree(new StyleNodeVisitor() {
                @Override
                public boolean visit(StyleNode node) {
                    int depth = node.getDepth();
                    for (int i = 0; i < depth; i++)
                        sb.append("  ");

                    sb.append(String.format("<%s ", node.getTag().getName()));
                    for (Map.Entry<StyleAttribute, String> entry : node.attrMap.entrySet()) {
                        sb.append(String.format("%s=\"%s\" ", entry.getKey().getName(), entry.getValue().toString()));
                    }
                    if (showCascadedAttribute) {
                        for (Map.Entry<StyleAttribute, Object> entry : node.contextAttrMap.entrySet()) {
                            sb.append(String.format("%s=\'%s\' ", entry.getKey().getName(), entry.getValue().toString()));
                        }
                    }

                    sb.append(">");

                    if (node.getChildren().size() > 0)
                        sb.append("\n");

                    String s = node.getText();
                    if (s != null) {
                        s = s.replace("\n", "\\n");
                        sb.append(s);
                    }

                    return true;
                }

                @Override
                public boolean visitTail(StyleNode node) {
                    int depth = node.getDepth();
                    if (node.getChildren().size() > 0) {
                        for (int i = 0; i < depth; i++)
                            sb.append("  ");
                    }
                    sb.append(String.format("</%s>\n", node.getTag().getName()));

                    return true;
                }
            });
        return sb.toString();
    }

}




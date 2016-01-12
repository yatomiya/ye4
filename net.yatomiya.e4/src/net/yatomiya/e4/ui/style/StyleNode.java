/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import java.util.*;
import java.util.function.*;
import org.eclipse.jface.text.*;
import net.yatomiya.e4.ui.style.viewer.*;
import net.yatomiya.e4.util.*;

public class StyleNode implements TreeNode<StyleNode>, IRegion {
    private StyleTag tag;
    private StyleNode parent;
    private List<StyleNode> children;
    private boolean isEnable;

    private Map<Object, Object> dataMap;
    private Map<StyleAttribute, String> attrMap;
    private Map<StyleAttribute, Object> csdAttrMap;

    private int docRegionOffset;
    private int docRegionLength;

    public StyleNode(StyleTag tag) {
        this.tag = tag;

        parent = null;
        children = Collections.EMPTY_LIST;
        isEnable = true;

        dataMap = Collections.EMPTY_MAP;
        attrMap = Collections.EMPTY_MAP;
        csdAttrMap = Collections.EMPTY_MAP;

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
        List<StyleNode> list = new ArrayList<>();
        for (StyleNode child : children) {
            if (child.isEnable())
                list.add(child);
        }
        return list;
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
        StyleNode rootNode = getRoot();
        StyleDocument doc = rootNode.getData(StyleDocument.class);
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
                n.cascadeAttributeAll();
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
                StyleNode dummy = StyleTag.TAG_SPAN.createStyleNode();
                dummy.children = new ArrayList<>();
                dummy.children.addAll(replaceNodeList);
                dummy.formatBlock();
                dummy.children.clear();
            }

            children.addAll(index, replaceNodeList);

            for (StyleNode n : replaceNodeList) {
                n.parent = this;
                n.cascadeAttributeAll();
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
        StyleNode rootNode = getRoot();
        StyleDocument doc = rootNode.getData(StyleDocument.class);
        if (doc == null)
            return;

        rootNode.calculateTextRegionTree();
        try {
            doc.replace(region.getOffset(), region.getLength(), replaceText);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        rootNode.formatBlock();
    }

    public String buildText() {
        StringBuilder sb = new StringBuilder();

        visitNodeTree(node -> {
                if (node instanceof TextStyleNode) {
                    String text = ((TextStyleNode)node).getText();
                    if (text != null) {
                        sb.append(text);
                    }
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
                    node.evalCascadedAttribute(attr);
                    return true;
                });

            if (attr.isPresentational()) {
                StyleViewer viewer = getCascadedData(StyleViewer.class);
                if (viewer != null) {
                    viewer.invalidateTextPresentation(getOffset(), getLength());
                }
            }
        }
    }

    protected void cascadeAttributeAll() {
        visitNodeTree(node -> {
                if (node.csdAttrMap.size() > 0)
                    node.csdAttrMap.clear();

                Set<StyleAttribute> set = new HashSet();
                if (node.getParent() != null && node.getParent().csdAttrMap.size() > 0)
                    set.addAll(node.getParent().csdAttrMap.keySet());
                if (node.attrMap.size() > 0)
                    set.addAll(node.attrMap.keySet());

                for (StyleAttribute attr : set) {
                    node.evalCascadedAttribute(attr);
                }
                return true;
            });
    }

    protected void evalCascadedAttribute(StyleAttribute attr) {
        Object value = null;
        String str = attrMap.get(attr);
        if (JUtils.isNotEmpty(str)) {
            value = attr.parseValue(str);
        }

        if (attr.isCascadable()) {
            Object cascadedValue = null;
            if (getParent() != null)
                cascadedValue = getParent().csdAttrMap.get(attr);

            if (cascadedValue == null) {
                // value is value.
            } else if (value == null) {
                value = cascadedValue;
            } else if (value == StyleAttribute.DEFAULT_VALUE) {
                value = null;
            } else {
                value = attr.applyCascadeValue(value, cascadedValue);
            }
        }

        if (value == null) {
            csdAttrMap.remove(attr);

            if (csdAttrMap.size() == 0)
                csdAttrMap = Collections.EMPTY_MAP;
        } else {
            if (csdAttrMap == Collections.EMPTY_MAP)
                csdAttrMap = new SmallMap<>();

            csdAttrMap.put(attr, value);
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

    public Object getCascadedAttribute(StyleAttribute attr) {
        return csdAttrMap.get(attr);
    }

    public Map<StyleAttribute, Object> getCascadedAttributeMap() {
        return Collections.unmodifiableMap(csdAttrMap);
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

    public Object getCascadedData(Object key) {
        StyleNode node = findAncestor(n -> n.getData(key) != null);
        if (node != null)
            return node.getData(key);
        return null;
    }

    public <T> T getCascadedData(Class<T> clazz) {
        return (T)getCascadedData((Object)clazz);
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
/*
  not implemented as for now.
    public void setEnable(boolean v) {
        if (isEnable != v) {
            boolean oldActive = isActive();

            isEnable = v;

            cascadeAttributeAll();

            StyleNode rootNode = getRoot();
            StyleDocument doc = rootNode.getData(StyleDocument.class);
            if (doc != null) {
                if (isEnable) {
                    if (isActive()) {
                        String nodeText = buildText();
                        StyleNode aboveNode = findUpstream(n -> (n != this) && (n.isActive()));
                        if (aboveNode != null) {
                            int offset = 0;
                            if (aboveNode == getParent())
                                offset = aboveNode.getOffset();
                            else
                                offset = aboveNode.getOffset() + aboveNode.getLength();

                            doDocumentReplace(new Region(offset, 0), nodeText);
                        }
                    }
                } else {
                    if (oldActive) {
                        doDocumentReplace(new Region(getOffset(), getLength()), "");
                    }
                }
            }
        }
    }

    public boolean isActive() {
        StyleNode node = this;
        while (node != null) {
            if (!node.isEnable())
                return false;
            node = node.getParent();
        }
        return true;
    }
*/
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

    public boolean visitUpstream(Predicate<StyleNode> visitor) {
        boolean result = true;

        result = visitor.test(this);

        if (result && getParent() != null) {
            int index = getParent().getChildren().indexOf(this);
            if (index == 0) {
                return getParent().visitUpstream(visitor);
            } else {
                StyleNode node = getParent().getChildren().get(index - 1);
                while (node.getChildren().size() > 0) {
                    node = node.getChildren().get(node.getChildren().size() - 1);
                }
                return node.visitUpstream(visitor);
            }
        }
        return result;
    }

    public void update() {
        getTag().update(this);
    }

    public void updateTree() {
        visitNodeTree(node -> {
                node.update();
                return true;
            });
    }


    private void calculateTextRegionTree() {
        visitNodeTree(new StyleNodeVisitor() {
                int offset = 0;

                @Override
                public boolean visit(StyleNode node) {
                    // save region starting offset
                    node.docRegionOffset = offset;
                    node.docRegionLength = 0;

                    if (node instanceof TextStyleNode) {
                        String text = ((TextStyleNode)node).getText();
                        if (JUtils.isNotEmpty(text)) {
                            offset += text.length();
                        }
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
            TextStyleNode lastTextNode;
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
                            && nodeChildren.get(0).getTag() == StyleTag.TAG_INTERNAL_NEWLINE_FOR_BLOCK) {
                            node.removeChild(0);
                        }
                    } else {
                        if (lastTextNode == null || !lastTextNode.isCancelNextBlockNewline()) {
                            lastLastChar = lastChar;
                            lastChar = '\n';

                            if (nodeChildren.size() == 0
                                || nodeChildren.get(0).getTag() != StyleTag.TAG_INTERNAL_NEWLINE_FOR_BLOCK) {
                                node.addChild(0, StyleTag.TAG_INTERNAL_NEWLINE_FOR_BLOCK.createStyleNode());
                            }
                        }
                    }
                } else {
                    String text = node instanceof TextStyleNode ? ((TextStyleNode)node).getText() : null;
                    if (JUtils.isNotEmpty(text)) {
                        lastTextNode = (TextStyleNode)node;
                        lastLastChar = lastChar;
                        lastChar = text.charAt(text.length() - 1);
                    }
                }

                for (StyleNode child : new ArrayList<>(node.getChildren())) {
                    format(child);
                }

                if (node.getTag().isBlock()) {
                    if (lastChar == '\n') {
                        if (lastLastChar == '\n') {
                            if (nodeChildren.size() > 0
                                && nodeChildren.get(nodeChildren.size() - 1).getTag() == StyleTag.TAG_INTERNAL_NEWLINE_FOR_BLOCK) {
                                node.removeChild(node.getChildren().size() - 1);
                            }
                        }
                    } else {
                        lastLastChar = lastChar;
                        lastChar = '\n';

                        node.addChild(StyleTag.TAG_INTERNAL_NEWLINE_FOR_BLOCK.createStyleNode());
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
                        sb.append(String.format("%s=\"%s\" ", entry.getKey().getName(), entry.getValue().toString(), 10));
                    }
                    if (showCascadedAttribute) {
                        for (Map.Entry<StyleAttribute, Object> entry : node.csdAttrMap.entrySet()) {
                            sb.append(String.format("%s=\'%s\' ", entry.getKey().getName(), entry.getValue().toString(), 10));
                        }
                    }

                    sb.append(">");

                    if (node.getChildren().size() > 0)
                        sb.append("\n");

                    if (node instanceof TextStyleNode) {
                        String s = ((TextStyleNode)node).getText();
                        if (s != null) {
                            s = s.replace("\n", "\\n");
                            sb.append(s);
                        }
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




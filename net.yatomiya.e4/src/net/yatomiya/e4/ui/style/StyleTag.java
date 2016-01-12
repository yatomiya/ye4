/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import org.jsoup.nodes.*;

public class StyleTag {
    private String name;
    private boolean isBlock;

    protected StyleTag(String name, boolean isBlock) {
        this.name = name;
        this.isBlock = isBlock;
    }

    public String getName() {
        return name;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public StyleNode createStyleNode() {
        return new StyleNode(this);
    }

    protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
    }

    public void update(StyleNode node) {
    }

    public static final TextStyleTag TAG_TEXT = new TextStyleTag("text");
    public static final ElementStyleTag TAG_SPAN = new ElementStyleTag("span", false);
    public static final ElementStyleTag TAG_P = new ElementStyleTag("p", true);
    public static final ElementStyleTag TAG_DIV = new ElementStyleTag("div", true);
    public static final ConstantStyleTag TAG_BR = new ConstantStyleTag("br", "\n");
    public static final ConstantStyleTag TAG_HR = new ConstantStyleTag("hr", "\n");
    public static final ElementStyleTag TAG_A = new ElementStyleTag("a", false);
    public static final ElementStyleTag TAG_H1 = new ElementStyleTag("h1", false);
    public static final ElementStyleTag TAG_H2 = new ElementStyleTag("h2", false);
    public static final ElementStyleTag TAG_H3 = new ElementStyleTag("h3", false);
    public static final ElementStyleTag TAG_H4 = new ElementStyleTag("h4", false);
    public static final ElementStyleTag TAG_H5 = new ElementStyleTag("h5", false);
    public static final ElementStyleTag TAG_H6 = new ElementStyleTag("h6", false);
    public static final ElementStyleTag TAG_EM = new ElementStyleTag("em", false);
    public static final ElementStyleTag TAG_STRONG = new ElementStyleTag("strong", false);
    public static final ElementStyleTag TAG_SUP = new ElementStyleTag("sup", false);
    public static final ElementStyleTag TAG_DEL = new ElementStyleTag("del", false);
    public static final ElementStyleTag TAG_BLOCKQUOTE = new ElementStyleTag("blockquote", false);
    public static final ElementStyleTag TAG_PRE = new ElementStyleTag("pre", false) {
            @Override
            protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                builder.pushDataMap();
                builder.setData(TextStyleTag.PREFORMATTED_KEY, Boolean.TRUE);
                super.parse(node, styleNode, builder);
                builder.popDataMap();
            }
        };
    public static final ElementStyleTag TAG_CODE = new ElementStyleTag("code", false);
    public static final ElementStyleTag TAG_TABLE = new ElementStyleTag("table", true);
    public static final ElementStyleTag TAG_THEAD = new ElementStyleTag("thead", true);
    public static final ElementStyleTag TAG_TBODY = new ElementStyleTag("tbody", true);
    public static final ElementStyleTag TAG_TR = new ElementStyleTag("tr", true);
    public static final ElementStyleTag TAG_TH = new ElementStyleTag("th", false);
    public static final ElementStyleTag TAG_TD = new ElementStyleTag("td", false);
    private static final String LI_INDEX_KEY = "__li_index_key__";
    public static final StyleTag TAG_OL = new ElementStyleTag("ol", true) {
            @Override
            protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer liIndex = (Integer)builder.getData(LI_INDEX_KEY);
                builder.setData(LI_INDEX_KEY, new Integer(0));
                super.parse(node, styleNode, builder);
                builder.setData(LI_INDEX_KEY, liIndex);

                Integer indentValue = null;
                try {
                    indentValue = Integer.valueOf(styleNode.getAttribute(StyleAttribute.ATTRIBUTE_INDENT));
                } catch (Throwable e) {
                    indentValue = null;
                }
                int indent = indentValue != null ? indentValue : 0;
                indent += 16;
                styleNode.setAttribute(StyleAttribute.ATTRIBUTE_INDENT, String.valueOf(indent));
            }
        };
    public static final StyleTag TAG_UL = new ElementStyleTag("ul", true) {
            @Override
            protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer liIndex = (Integer)builder.getData(LI_INDEX_KEY);
                builder.setData(LI_INDEX_KEY, null);
                super.parse(node, styleNode, builder);
                builder.setData(LI_INDEX_KEY, liIndex);

                Integer indentValue = null;
                try {
                    indentValue = Integer.valueOf(styleNode.getAttribute(StyleAttribute.ATTRIBUTE_INDENT));
                } catch (Throwable e) {
                    indentValue = null;
                }
                int indent = indentValue != null ? indentValue : 0;
                indent += 16;
                styleNode.setAttribute(StyleAttribute.ATTRIBUTE_INDENT, String.valueOf(indent));
            }
        };
    public static final StyleTag TAG_LI = new ElementStyleTag("li", true) {
            @Override
            protected boolean buildOpenTag(Element element, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer index = (Integer)builder.getData(LI_INDEX_KEY);
                if (index != null) {
                    index += 1;
                    builder.setData(LI_INDEX_KEY, Integer.valueOf(index));

                    TextStyleNode lnode = TAG_TEXT.createStyleNode();
                    lnode.setText(String.format("%d. ", index));
                    lnode.setCancelNextBlockNewline(true);
                    styleNode.addChild(lnode);
                }
                return true;
            }
        };
    public static final StyleTag TAG_INTERNAL_COMMENT = new StyleTag("internal_comment", false);
    public static final StyleTag TAG_INTERNAL_NEWLINE_FOR_BLOCK = new ConstantStyleTag("internal_newline_for_block", "\n");

    private static StyleTag[] standardTags = new StyleTag[] {
        TAG_TEXT,
        TAG_SPAN,
        TAG_P,
        TAG_DIV,
        TAG_BR,
        TAG_HR,
        TAG_A,
        TAG_H1,
        TAG_H2,
        TAG_H3,
        TAG_H4,
        TAG_H5,
        TAG_H6,
        TAG_EM,
        TAG_STRONG,
        TAG_SUP,
        TAG_DEL,
        TAG_PRE,
        TAG_CODE,
        TAG_BLOCKQUOTE,
        TAG_TABLE,
        TAG_THEAD,
        TAG_TBODY,
        TAG_TR,
        TAG_TH,
        TAG_TD,
        TAG_OL,
        TAG_UL,
        TAG_LI,
        TAG_INTERNAL_COMMENT,
        TAG_INTERNAL_NEWLINE_FOR_BLOCK,
    };

    public static StyleTag[] getStandardTags() {
        return standardTags.clone();
    }
}



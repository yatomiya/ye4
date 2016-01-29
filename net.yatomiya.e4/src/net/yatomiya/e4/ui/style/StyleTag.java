/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import org.jsoup.nodes.*;
import net.yatomiya.e4.util.*;

public class StyleTag {
    private String name;
    private boolean isBlock;

    public StyleTag(String name, boolean isBlock) {
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

    public void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
    }

    public static final StyleTag TEXT = new StyleTag("text", false) {
            @Override
            public void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                String v = styleNode.getAttribute(StyleAttribute.VALUE);
                if (!JUtils.isEmpty(v)) {
                    styleNode.setText(v);
                }
            }
        };
    public static final ElementStyleTag SPAN = new ElementStyleTag("span", false);
    public static final ElementStyleTag P = new ElementStyleTag("p", true);
    public static final ElementStyleTag DIV = new ElementStyleTag("div", true);
    public static final ConstantStyleTag BR = new ConstantStyleTag("br", "\n");
    public static final ConstantStyleTag HR = new ConstantStyleTag("hr", "\n");
    public static final ElementStyleTag A = new ElementStyleTag("a", false);
    public static final ElementStyleTag H1 = new ElementStyleTag("h1", false);
    public static final ElementStyleTag H2 = new ElementStyleTag("h2", false);
    public static final ElementStyleTag H3 = new ElementStyleTag("h3", false);
    public static final ElementStyleTag H4 = new ElementStyleTag("h4", false);
    public static final ElementStyleTag H5 = new ElementStyleTag("h5", false);
    public static final ElementStyleTag H6 = new ElementStyleTag("h6", false);
    public static final ElementStyleTag EM = new ElementStyleTag("em", false);
    public static final ElementStyleTag STRONG = new ElementStyleTag("strong", false);
    public static final ElementStyleTag SUP = new ElementStyleTag("sup", false);
    public static final ElementStyleTag DEL = new ElementStyleTag("del", false);
    public static final ElementStyleTag BLOCKQUOTE = new ElementStyleTag("blockquote", false);
    public static final ElementStyleTag PRE = new ElementStyleTag("pre", false) {
            @Override
            public void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                builder.pushDataMap();
                builder.setData(InternalHtmlTextStyleTag.PREFORMATTED_KEY, Boolean.TRUE);
                super.parse(node, styleNode, builder);
                builder.popDataMap();
            }
        };
    public static final ElementStyleTag CODE = new ElementStyleTag("code", false);
    public static final ElementStyleTag TABLE = new ElementStyleTag("table", true);
    public static final ElementStyleTag THEAD = new ElementStyleTag("thead", true);
    public static final ElementStyleTag TBODY = new ElementStyleTag("tbody", true);
    public static final ElementStyleTag TR = new ElementStyleTag("tr", true);
    public static final ElementStyleTag TH = new ElementStyleTag("th", false);
    public static final ElementStyleTag TD = new ElementStyleTag("td", false);
    private static final String LI_INDEX_KEY = "__li_index_key__";
    public static final StyleTag OL = new ElementStyleTag("ol", true) {
            @Override
            public void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer liIndex = (Integer)builder.getData(LI_INDEX_KEY);
                builder.setData(LI_INDEX_KEY, new Integer(0));
                super.parse(node, styleNode, builder);
                builder.setData(LI_INDEX_KEY, liIndex);

                Integer indentValue = null;
                try {
                    indentValue = Integer.valueOf(styleNode.getAttribute(StyleAttribute.INDENT));
                } catch (Throwable e) {
                    indentValue = null;
                }
                int indent = indentValue != null ? indentValue : 0;
                indent += 16;
                styleNode.setAttribute(StyleAttribute.INDENT, String.valueOf(indent));
            }
        };
    public static final StyleTag UL = new ElementStyleTag("ul", true) {
            @Override
            public void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer liIndex = (Integer)builder.getData(LI_INDEX_KEY);
                builder.setData(LI_INDEX_KEY, null);
                super.parse(node, styleNode, builder);
                builder.setData(LI_INDEX_KEY, liIndex);

                Integer indentValue = null;
                try {
                    indentValue = Integer.valueOf(styleNode.getAttribute(StyleAttribute.INDENT));
                } catch (Throwable e) {
                    indentValue = null;
                }
                int indent = indentValue != null ? indentValue : 0;
                indent += 16;
                styleNode.setAttribute(StyleAttribute.INDENT, String.valueOf(indent));
            }
        };
    public static final StyleTag LI = new ElementStyleTag("li", true) {
            @Override
            protected boolean buildOpenTag(Element element, StyleNode styleNode, StyleNodeBuilder builder) {
                Integer index = (Integer)builder.getData(LI_INDEX_KEY);
                if (index != null) {
                    index += 1;
                    builder.setData(LI_INDEX_KEY, Integer.valueOf(index));

                    StyleNode lnode = TEXT.createStyleNode();
                    lnode.setText(String.format("%d. ", index));
                    lnode.setCancelNextBlockNewline(true);
                    styleNode.addChild(lnode);
                }
                return true;
            }
        };
    public static final StyleTag INTERNAL_HTML_TEXT = new InternalHtmlTextStyleTag("internal_html_text");
    public static final StyleTag INTERNAL_HTML_COMMENT = new StyleTag("internal_html_comment", false);
    public static final StyleTag INTERNAL_NEWLINE_FOR_BLOCK = new ConstantStyleTag("internal_newline_for_block", "\n");

    private static StyleTag[] standardTags = new StyleTag[] {
        TEXT,
        SPAN,
        P,
        DIV,
        BR,
        HR,
        A,
        H1,
        H2,
        H3,
        H4,
        H5,
        H6,
        EM,
        STRONG,
        SUP,
        DEL,
        PRE,
        CODE,
        BLOCKQUOTE,
        TABLE,
        THEAD,
        TBODY,
        TR,
        TH,
        TD,
        OL,
        UL,
        LI,
        INTERNAL_HTML_TEXT,
        INTERNAL_HTML_COMMENT,
        INTERNAL_NEWLINE_FOR_BLOCK,
    };

    public static StyleTag[] getStandardTags() {
        return standardTags.clone();
    }
}



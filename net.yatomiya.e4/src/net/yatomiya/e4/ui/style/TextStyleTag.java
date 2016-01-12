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

public class TextStyleTag extends StyleTag {
    public static final String PREFORMATTED_KEY = TextStyleTag.class.getName() + ":preformatted_key";

    public TextStyleTag(String tag) {
        super(tag, false);
    }

    @Override
    public TextStyleNode createStyleNode() {
        return new TextStyleNode(this);
    }

    @Override
    protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
        if (node instanceof TextNode) {
            Boolean pre = (Boolean)builder.getData(PREFORMATTED_KEY);
            String text = null;
            if (pre != null && pre) {
                text = ((TextNode)node).getWholeText();
            } else {
                text = parseNodeText((TextNode)node, builder);
            }
            if (text != null)
                ((TextStyleNode)styleNode).setText(text);
        }
    }

    private String parseNodeText(TextNode node, StyleNodeBuilder builder) {
        // TextNode.text()内部で、複数の連続するスペースはひとつのスペースに変換される。
        String text = node.text();

        // 文頭、文末の空白の削除チェック
        if (text.length() > 0 && text.charAt(0) == ' ') {
            boolean trimHead = false;
            Node prevNode = node.previousSibling();
            if (prevNode == null) {
                trimHead = true;
            } else if (prevNode instanceof Element) {
                Element e = (Element)prevNode;
                StyleTag tag = builder.getStyleTag(e);
                if (tag.isBlock() || tag == StyleTag.TAG_BR)
                    trimHead = true;
            }
            if (trimHead)
                text = StringUtils.trimHeadWhitespace(text);
        }
        if (text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
            boolean trimTail = false;
            Node nextNode = node.nextSibling();
            if (nextNode == null) {
                trimTail = true;
            } else if (nextNode instanceof Element) {
                Element e = (Element)nextNode;
                StyleTag tag = builder.getStyleTag(e);
                if (tag.isBlock() || tag == StyleTag.TAG_BR)
                    trimTail = true;
            }
            if (trimTail)
                text = StringUtils.trimTailWhitespace(text);
        }

        return text;
    }

}


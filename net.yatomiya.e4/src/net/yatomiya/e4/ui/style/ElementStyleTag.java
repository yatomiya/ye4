/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import org.jsoup.nodes.*;



public class ElementStyleTag extends StyleTag {
    public ElementStyleTag(String tag, boolean isBlock) {
        super(tag, isBlock);
    }

    @Override
    protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
        Element e = (Element)node;
        boolean doChildren = buildOpenTag(e, styleNode, builder);
        if (doChildren) {
            styleNode.addChildAll(builder.build(e.childNodes()));
        }

        buildCloseTag(e, styleNode, null);
    }

    protected boolean buildOpenTag(Element element, StyleNode styleNode, StyleNodeBuilder builder) {
        return true;
    }

    protected void buildCloseTag(Element element, StyleNode styleNode, StyleNodeBuilder builder) {
    }

}

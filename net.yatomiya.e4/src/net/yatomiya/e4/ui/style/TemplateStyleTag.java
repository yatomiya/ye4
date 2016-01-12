/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import org.jsoup.nodes.*;

public class TemplateStyleTag extends StyleTag {
    private String template;

    public TemplateStyleTag(String name) {
        super(name, false);
    }

    @Override
    protected void parse(Node node, StyleNode styleNode, StyleNodeBuilder builder) {
        StyleNode templateNode = builder.buildTemplate(getName());
        if (templateNode != null) {
            styleNode.addChild(templateNode);
        }
/*
            // もしテンプレートに子ノードがなければ、元ノードの子ノードをテンプレートの下にぶらさげる。
            if (templateNode.childNodeSize() == 0) {
                snode.addChildAll(builder.build(node.childNodes()));
            }
*/
    }
}


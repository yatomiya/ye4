/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import net.yatomiya.e4.ui.style.viewer.*;
import net.yatomiya.e4.ui.viewer.*;

public class TextStyleNode extends StyleNode {
    private String text;
    private boolean cancelNextBlockNewline;

    public TextStyleNode(StyleTag tag) {
        this(tag, "");
    }

    public TextStyleNode(StyleTag tag, String text) {
        super(tag);

        cancelNextBlockNewline = false;

        setText(text);
    }

    public void setText(String text) {
        if (getChildren().size() > 0)
            throw new IllegalStateException();
        if (text == null)
            throw new NullPointerException();

        this.text = text;

        StyleNode rootNode = getRoot();
        StyleDocument doc = rootNode.getData(StyleDocument.class);
        if (doc != null) {
            doDocumentReplace(new MutableRegion(getOffset(), getLength()), text);
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
}


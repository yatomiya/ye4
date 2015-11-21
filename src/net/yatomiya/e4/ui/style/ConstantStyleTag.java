/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

class ConstantStyleTag extends StyleTag {
    private String value;

    public ConstantStyleTag(String tag, String value) {
        super(tag, false);
        this.value = value;
    }

    @Override
    public TextStyleNode createStyleNode() {
        return new TextStyleNode(this, value);
    }
}


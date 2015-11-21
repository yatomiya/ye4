/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import org.eclipse.jface.text.*;

public class MutableRegion implements IRegion {
    private int offset;
    private int length;

    public MutableRegion() {
        this(0, 0);
    }

    public MutableRegion(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLength() {
        return length;
    }

    public int getEnd() {
        return offset + length - 1;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void set(int offset, int length) {
        setOffset(offset);
        setLength(length);
    }
}

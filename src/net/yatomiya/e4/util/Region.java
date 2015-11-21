/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

public class Region {
    private int offset;
    private int length;

    public Region(int offset, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length must be greater than 0.");
        this.offset = offset;
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public int getEnd() {
        return offset + length - 1;
    }

    public boolean contains(int v) {
        return JUtils.contains(getOffset(), getLength(), v);
    }

    public boolean intersects(int otherOffset, int otherLength) {
        return JUtils.intersects(getOffset(), getLength(), otherOffset, otherLength);
    }

    public boolean intersects(Region region) {
        return JUtils.intersects(getOffset(), getLength(), region.getOffset(), region.getLength());
    }
}

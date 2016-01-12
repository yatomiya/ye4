/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;

public class BitSetIterator implements Iterator<Integer> {
    private BitSet bitSet;
    private boolean findSetBit;
    private int index;

    public BitSetIterator(BitSet bitSet) {
        this(bitSet, true);
    }

    public BitSetIterator(BitSet bitSet, boolean findSetBit) {
        this.bitSet = bitSet;
        this.findSetBit = findSetBit;
        index = 0;
    }

    @Override
    public Integer next() {
        if (findSetBit)
            index = bitSet.nextSetBit(index + 1);
        else
            index = bitSet.nextClearBit(index + 1);
        if (index < 0)
            throw new NoSuchElementException();
        return index;
    }

    @Override
    public boolean hasNext() {
        if (findSetBit)
            return bitSet.nextSetBit(index + 1) >= 0;
        else
            return bitSet.nextClearBit(index + 1) >= 0;
    }

    public static Iterable<Integer> iterate(BitSet bitSet) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new BitSetIterator(bitSet);
            }
        };
    }
}

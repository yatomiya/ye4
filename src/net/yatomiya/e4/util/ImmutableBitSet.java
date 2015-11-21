/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.nio.*;
import java.util.*;

public class ImmutableBitSet implements Cloneable, Serializable, Iterable<Integer> {
    private final BitSet fBitSet;

    public ImmutableBitSet() {
        fBitSet = new BitSet(1);
    }

    public ImmutableBitSet(BitSet bitSet) {
        fBitSet = (BitSet)bitSet.clone();
    }

    private BitSet getBitSet() {
        return fBitSet;
    }

    public BitSet toBitSet() {
        return (BitSet)fBitSet.clone();
    }

    public ImmutableBitSet and(ImmutableBitSet v) {
        BitSet bs = toBitSet();
        bs.and(v.getBitSet());
        return new ImmutableBitSet(bs);
    }

    public ImmutableBitSet andNot(ImmutableBitSet v) {
        BitSet bs = toBitSet();
        bs.andNot(v.getBitSet());
        return new ImmutableBitSet(bs);
    }

    public int cardinality() {
        return getBitSet().cardinality();
    }

    public ImmutableBitSet clear() {
        return new ImmutableBitSet();
    }

    public ImmutableBitSet clear(int bitIndex) {
        BitSet bs = toBitSet();
        bs.clear(bitIndex);
        return new ImmutableBitSet(bs);
    }

    public ImmutableBitSet clear(int fromIndex, int toIndex) {
        BitSet bs = toBitSet();
        bs.clear(fromIndex, toIndex);
        return new ImmutableBitSet(bs);
    }

    @Override
    public Object clone() {
        return new ImmutableBitSet(fBitSet);
    }

    public boolean get(int bitIndex) {
        return fBitSet.get(bitIndex);
    }

    public ImmutableBitSet get(int fromIndex, int toIndex) {
        return new ImmutableBitSet(fBitSet.get(fromIndex, toIndex));
    }

    public boolean intersects(ImmutableBitSet bs) {
        return getBitSet().intersects(bs.getBitSet());
    }

    public boolean isEmpty() {
        return getBitSet().isEmpty();
    }

    public int length() {
        return getBitSet().length();
    }

    public int nextClearBit(int fromIndex) {
        return getBitSet().nextClearBit(fromIndex);
    }

    public int nextSetBit(int fromIndex) {
        return getBitSet().nextSetBit(fromIndex);
    }

    public ImmutableBitSet or(ImmutableBitSet v) {
        BitSet bs = toBitSet();
        bs.or(v.getBitSet());
        return new ImmutableBitSet(bs);
    }

    public int previousClearBit(int fromIndex) {
        return getBitSet().previousClearBit(fromIndex);
    }

    public int previousSetBit(int fromIndex) {
        return getBitSet().previousSetBit(fromIndex);
    }

    public ImmutableBitSet set(int bitIndex) {
        BitSet bs = toBitSet();
        bs.set(bitIndex);
        return new ImmutableBitSet(bs);
    }

    public ImmutableBitSet set(int bitIndex, boolean value) {
        BitSet bs = toBitSet();
        bs.set(bitIndex, value);
        return new ImmutableBitSet(bs);
    }

    public ImmutableBitSet set(int fromIndex, int toIndex) {
        BitSet bs = toBitSet();
        bs.set(fromIndex, toIndex);
        return new ImmutableBitSet(bs);
    }

    public ImmutableBitSet set(int fromIndex, int toIndex, boolean value) {
        BitSet bs = toBitSet();
        bs.set(fromIndex, toIndex, value);
        return new ImmutableBitSet(bs);
    }

    public int size() {
        return getBitSet().size();
    }

    public byte[] toByteArray() {
        return getBitSet().toByteArray();
    }

    public long[] toLongArray() {
        return getBitSet().toLongArray();
    }

    @Override
    public String toString() {
        return getBitSet().toString();
    }

    public static ImmutableBitSet valueOf(byte[] bytes) {
        return new ImmutableBitSet(BitSet.valueOf(bytes));
    }

    public static ImmutableBitSet valueOf(ByteBuffer bb) {
        return new ImmutableBitSet(BitSet.valueOf(bb));
    }

    public static ImmutableBitSet valueOf(long[] longs) {
        return new ImmutableBitSet(BitSet.valueOf(longs));
    }

    public static ImmutableBitSet valueOf(LongBuffer lb) {
        return new ImmutableBitSet(BitSet.valueOf(lb));
    }

    public ImmutableBitSet xor(ImmutableBitSet v) {
        BitSet bs = toBitSet();
        bs.xor(v.getBitSet());
        return new ImmutableBitSet(bs);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new InternalIterator(true);
    }

    private class InternalIterator implements Iterator<Integer> {
        boolean setOrClear;
        int index;

        InternalIterator(boolean setOrClear) {
            this.setOrClear = setOrClear;
            index = 0;
        }

        @Override
        public Integer next() {
            if (setOrClear)
                index = getBitSet().nextSetBit(index + 1);
            else
                index = getBitSet().nextClearBit(index + 1);
            if (index < 0)
                throw new NoSuchElementException();
            return index;
        }

        @Override
        public boolean hasNext() {
            if (setOrClear)
                return getBitSet().nextSetBit(index + 1) >= 0;
            else
                return getBitSet().nextClearBit(index + 1) >= 0;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fBitSet == null) ? 0 : fBitSet.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImmutableBitSet other = (ImmutableBitSet)obj;
        if (fBitSet == null) {
            if (other.fBitSet != null)
                return false;
        } else if (!fBitSet.equals(other.fBitSet))
            return false;
        return true;
    }
}

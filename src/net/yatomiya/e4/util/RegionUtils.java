/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;

public class RegionUtils {
    public static <T extends Region> T find(T[] array, int offset) {
        if (array.length == 0)
            return null;

        int start = array[0].getOffset();
        int end = array[array.length - 1].getOffset() + array[array.length - 1].getLength() - 1;
        if (offset < start || end < offset)
            return null;

        int index = Arrays.binarySearch(
            array, new Region(offset, 1),
            (r, key) -> {
                if (r.contains(key.getOffset()))
                    return 0;
                return r.getOffset() - key.getOffset();
            });

        if (index >= 0)
            return array[index];

        return null;
    }

    // array must be sorted by offset, not overlapped by others.
    public static <T extends Region> T[] find(T[] array, int offset, int length) {
        Class<T> componentType = (Class<T>)array.getClass().getComponentType();

        if (array.length == 0)
            return CUtils.createArray(componentType, 0);

        int start = array[0].getOffset();
        int end = array[array.length - 1].getOffset() + array[array.length - 1].getLength() - 1;
        if (offset + length - 1 < start || end < offset)
            return CUtils.createArray(componentType, 0);
        if (offset <= start && end < offset + length)
            return array.clone();

        List<T> list = new ArrayList<>(array.length);
        int startIndex = Arrays.binarySearch(
            array, new Region(offset, length),
            new Comparator<Region>() {
                    @Override
                    public int compare(Region r, Region key) {
                        if (r.intersects(key))
                            return 0;
                        return r.getOffset() - key.getOffset();
                    }
                });
        if (startIndex < 0)
            return CUtils.createArray(componentType, 0);
        for (int i = startIndex; i < array.length; i++) {
            T r = array[i];
            if (r.intersects(offset, length)) {
                list.add(r);
            } else if (list.size() > 0) {
                break;
            }
        }
        return list.toArray(CUtils.createArray(componentType, list.size()));
    }

    public static final Comparator<Region> OFFSET_COMPARATOR = new Comparator<Region>() {
        @Override
        public int compare(Region r1, Region r2) {
            if (r1.getOffset() < r2.getOffset())
                return -1;
            else if (r1.getOffset() == r2.getOffset())
                return 0;
            else
                return 1;
        }
    };

    public static int checkConsistency(Region[] regions) {
        Region prev = null;
        for (int i = 0; i < regions.length; i++) {
            Region r = regions[i];
            if (prev != null) {
                if (prev.getOffset() + prev.getLength() != r.getOffset()) {
                    return i - 1;
                }
                prev = r;
            }
        }
        return -1;
    }

}

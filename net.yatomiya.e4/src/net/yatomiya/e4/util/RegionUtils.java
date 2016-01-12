/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;
import org.eclipse.jface.text.*;

public class RegionUtils {
    public static boolean contains(IRegion region, int offset) {
        return JUtils.contains(region.getOffset(), region.getLength(), offset);
    }

    public static boolean overlaps(IRegion region, int offset, int length) {
        return JUtils.intersects(region.getOffset(), region.getLength(), offset, length);
    }

    public static boolean overlaps(IRegion r0, IRegion r1) {
        return JUtils.intersects(r0.getOffset(), r0.getLength(), r1.getOffset(), r1.getLength());
    }

    public static <T extends IRegion> T find(T[] array, int offset) {
        if (array.length == 0)
            return null;

        int start = array[0].getOffset();
        int end = array[array.length - 1].getOffset() + array[array.length - 1].getLength() - 1;
        if (offset < start || end < offset)
            return null;

        int index = Arrays.binarySearch(
            array, new Region(offset, 1),
            (r, key) -> {
                if (contains(r, key.getOffset()))
                    return 0;
                return r.getOffset() - key.getOffset();
            });

        if (index >= 0)
            return array[index];

        return null;
    }

    public static <T extends IRegion> T find(List<T> list, int offset) {
        IRegion r = find(list.toArray(new IRegion[list.size()]), offset);
        return (T)r;
    }

    // array must be sorted by offset, not overlapped by others.
    public static <T extends IRegion> List<T> find(List<T> list, int offset, int length) {
        if (list.size() == 0)
            return Collections.EMPTY_LIST;

        int start = list.get(0).getOffset();
        int end = list.get(list.size() - 1).getOffset() + list.get(list.size() - 1).getLength() - 1;
        if (offset + length - 1 < start || end < offset)
            return Collections.EMPTY_LIST;
        if (offset <= start && end < offset + length)
            return new ArrayList<>(list);

        IRegion[] array = list.toArray(new IRegion[list.size()]);
        IRegion target = new Region(offset, length);
        int startIndex = Arrays.binarySearch(
            array, target,
            new Comparator<IRegion>() {
                    @Override
                    public int compare(IRegion r, IRegion key) {
                        if (overlaps(r, key))
                            return 0;
                        return r.getOffset() - key.getOffset();
                    }
                });
        if (startIndex < 0)
            return Collections.EMPTY_LIST;

        List<T> result = new ArrayList<>(list.size());
        for (int i = startIndex; i < list.size(); i++) {
            T r = list.get(i);
            if (overlaps(r, offset, length)) {
                result.add(r);
            } else if (result.size() > 0) {
                break;
            }
        }
        return result;
    }

    public static final Comparator<IRegion> OFFSET_COMPARATOR = new Comparator<IRegion>() {
        @Override
        public int compare(IRegion r1, IRegion r2) {
            if (r1.getOffset() < r2.getOffset())
                return -1;
            else if (r1.getOffset() == r2.getOffset())
                return 0;
            else
                return 1;
        }
    };

    public static int checkConsistency(IRegion[] regions) {
        IRegion prev = null;
        for (int i = 0; i < regions.length; i++) {
            IRegion r = regions[i];
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


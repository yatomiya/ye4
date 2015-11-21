/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.util.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Region;
import net.yatomiya.e4.util.*;

public class JFaceTextUtils {
    private JFaceTextUtils() {
    }

    public static boolean contains(IRegion region, int offset) {
        return JUtils.contains(region.getOffset(), region.getLength(), offset);
    }

    public static boolean overlaps(IRegion region, int offset, int length) {
        return JUtils.intersects(region.getOffset(), region.getLength(), offset, length);
    }

    public static boolean overlaps(IRegion r0, IRegion r1) {
        return JUtils.intersects(r0.getOffset(), r0.getLength(), r1.getOffset(), r1.getLength());
    }

    public static <T extends IRegion> T find(List<T> tokenList, int offset) {
        if (tokenList.size() == 0)
            return null;

        int start = tokenList.get(0).getOffset();
        IRegion endRegion = tokenList.get(tokenList.size() - 1);
        int end = endRegion.getOffset() + endRegion.getLength() - 1;
        if (offset < start || end < offset)
            return null;

        int index = Collections.binarySearch(
            tokenList, new Region(offset, 1),
            new Comparator<IRegion>() {
                    @Override
                    public int compare(IRegion r, IRegion key) {
                        if (contains(r, key.getOffset()))
                            return 0;
                        return r.getOffset() - key.getOffset();
                    }
            });
        if (index >= 0)
            return tokenList.get(index);

        return null;
    }

    // array must be sorted by offset, not overlapped by others.
    public static <T extends IRegion> List<T> find(List<T> tokenList, int offset, int length) {
        List<T> list = new ArrayList<T>();

        if (tokenList.size() == 0)
            return list;

        int start = tokenList.get(0).getOffset();
        IRegion endRegion = tokenList.get(tokenList.size() - 1);
        int end = endRegion.getOffset() + endRegion.getLength() - 1;

        if (offset + length - 1 < start || end < offset)
            return list;
        if (offset <= start && end < offset + length) {
            list.addAll(tokenList);
            return list;
        }

        int startIndex = Collections.binarySearch(
            tokenList, new Region(offset, length),
            new Comparator<IRegion>() {
                    @Override
                    public int compare(IRegion r, IRegion key) {
                        if (overlaps(r, key))
                            return 0;
                        return r.getOffset() - key.getOffset();
                    }
                });

        if (startIndex < 0)
            return list;

        // find minimum index overlaps.
        while (startIndex > 0 && overlaps(tokenList.get(startIndex - 1), offset, length)) {
            startIndex--;
        }

        for (int i = startIndex; i < tokenList.size(); i++) {
            T r = tokenList.get(i);
            if (overlaps(r, offset, length)) {
                list.add(r);
            } else if (list.size() > 0) {
                break;
            }
        }
        return list;
    }
/*
    public static List<TokenRegion> fillTokenGap(List<TokenRegion> list, int startOffset, int length, TokenType tokenType) {
        List<TokenRegion> filledList = new ArrayList<>(list.size() * 2);
        int offset = startOffset;
        for (TokenRegion r : list) {
            if (offset < r.getOffset()) {
                filledList.add(new TokenRegion(offset, r.getOffset() - offset, tokenType));
            }
            filledList.add(r);
            offset = r.getOffset() + r.getLength();
        }
        if (offset < length) {
            filledList.add(new TokenRegion(offset, length - offset, tokenType));
        }
        return filledList;
    }
*/
    public static boolean checkRegionConsistency(List<? extends IRegion> regions, int offset, int length) {
        if (JUtils.isEmpty(regions))
            return true;

        if (checkRegionConsistency(regions)) {
            IRegion end = regions.get(regions.size() - 1);
            return regions.get(0).getOffset() == offset
                && (end.getOffset() + end.getLength() - regions.get(0).getOffset()) == length;
        }
        return false;
    }

    public static boolean checkRegionConsistency(List<? extends IRegion> regions) {
        IRegion prev = null;
        for (IRegion r : regions) {
            if (prev != null) {
                if (prev.getOffset() + prev.getLength() != r.getOffset()) {
                    return false;
                }
                prev = r;
            }
        }
        return true;
    }

    public static void translate(List<? extends MutableRegion> list, int offset) {
        for (MutableRegion r : list) {
            r.setOffset(r.getOffset() + offset);
        }
    }
}


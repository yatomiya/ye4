/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.lang.reflect.*;
import java.util.*;

public class JUtils {
    public static void println(Object o) {
        System.out.println(o);
    }

    public static void println(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static int getIntFrom(Map<String, String> map, String key, int defaultValue) {
        int v = defaultValue;
        if (map != null) {
            String str = map.get(key);
            if (str != null) {
                try {
                    v = Integer.valueOf(str);
                } catch (NumberFormatException e) {
                    // just skip
                }
            }
        }
        return v;
    }

    public static boolean nullEquals(Object s1, Object s2) {
        if (s1 == null || s2 == null)
            return s1 == s2;
        return s1.equals(s2);
    }

    public static boolean nonNull(Boolean b) {
        return b == null ? false : b;
    }

    public static int nonNull(Integer i) {
        return i == null ? 0 : i;
    }

    public static long nonNull(Long l) {
        return l == null ? 0: l;
    }

    public static String nonNull(String s) {
        return s == null ? "" : s;
    }

    public static <T> List<T> nonNull(List<T> list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static int clamp(int v, int min, int max) {
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    public static int clampLoop(int v, int min, int max) {
        if (min == max)
            return min;
        int range = max - min;
        if (v < min)
            v = max + 1 - ((min - v) % range);
        if (v > max)
            v = min - 1 + ((v - max) % range);
        return v;
    }

    public static boolean contains(int start, int length, int v) {
        if (length == 0)
            length = 1;

        int min = Math.min(start, start + length);
        int max = Math.max(start, start + length);
        return min <= v && v < max;
    }

    public static boolean intersects(int start1, int length1, int start2, int length2) {
        if (length1 == 0)
            length1 = 1;
        if (length2 == 0)
            length2 = 1;
        int min1 = Math.min(start1, start1 + length1);
        int max1 = Math.max(start1, start1 + length1);
        int min2 = Math.min(start2, start2 + length2);
        int max2 = Math.max(start2, start2 + length2);

        return (min1 < max2) && (min2 < max1);
    }

    public static int[] intersection(int start1, int length1, int start2, int length2) {
        if (intersects(start1, length1, start2, length2)) {
            int s = Math.min(start1, start2);
            int e = Math.max(start1 + length1 - 1, start2 + length2 - 1);
            return new int[] { s, e - s + 1 };
        } else {
            return new int[] { 0, 0 };
        }
    }

    public static int[] toIntArray(BitSet bitSet) {
        int[] array = new int[bitSet.cardinality()];
        int i = 0;
        for (int b = bitSet.nextSetBit(0); b >= 0; b = bitSet.nextSetBit(b + 1)) {
            array[i++] = b;
        }
        return array;
    }

    public static int sign(int v) {
        if (v == 0)
            return 0;
        if (v > 0)
            return 1;
        return -1;
    }

    public static int sign(long v) {
        return sign((int)v);
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?> ... parameters) {
        while (clazz != null) {
            Method m;
            try {
                m = clazz.getMethod(name, parameters);
                if (m != null)
                    return m;
            } catch (NoSuchMethodException e) {
            }

            try {
                m = clazz.getDeclaredMethod(name, parameters);
                if (m != null)
                    return m;
            } catch (NoSuchMethodException e) {
            }

            clazz = clazz.getSuperclass();
        }
        return null;
    }
}


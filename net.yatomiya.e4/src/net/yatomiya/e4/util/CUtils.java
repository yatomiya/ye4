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
import java.util.function.*;
import java.util.stream.*;

public class CUtils {
    public static <T> List<T> toList(T[] array) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }

    public static <T, U> T[] convertArray(U[] src, Class<? extends T> destClass) {
        T[] newArray = createArray(destClass, src.length);
        System.arraycopy(src, 0, newArray, 0, src.length);
        return newArray;
    }

    public static <T> T[] createArray(Class<T> clazz, int size) {
        return (T[])Array.newInstance(clazz, size);
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public static <T> T getFirst(List<T> c) {
        if (c == null || c.size() == 0)
            return null;
        return c.get(0);
    }

    public static boolean isAllSameClass(Object[] array, Class targetClass) {
        return isAllSameClass(Arrays.asList(array), targetClass);
    }

    public static boolean isAllSameClass(Collection<?> list, Class targetClass) {
        return findFirst(list, e -> targetClass.getClass() != e.getClass()) == null;
    }

    public static boolean isAllInstanceof(Object[] array, Class targetClass) {
        return isAllInstanceof(Arrays.asList(array), targetClass);
    }

    public static boolean isAllInstanceof(Collection<?> list, Class targetClass) {
        return findFirst(list, e -> !targetClass.isInstance(e)) == null;
    }

    public static <T> void forEach(Collection<? extends T> list, Consumer<T> func) {
        list.stream().forEach(func);
    }

    public static <T> T findFirst(Collection<? extends T> list, Predicate<T> func) {
        Optional<? extends T> found = list.stream().filter(func).findFirst();
        return found.isPresent() ? found.get() : null;
    }

    public static <T> List<T> filter(Collection<? extends T> list, Predicate<T> func) {
        return list.stream().filter(func).collect(Collectors.toList());
    }

    public static <T> List<T> distinct(Collection<? extends T> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public static <T> boolean anyMatch(Collection<? extends T> list, Predicate<T> func) {
        return list.stream().anyMatch(func);
    }

    public static <T> boolean allMatch(Collection<? extends T> list, Predicate<T> func) {
        return list.stream().allMatch(func);
    }

    public static <T> boolean noneMatch(Collection<? extends T> list, Predicate<T> func) {
        return list.stream().noneMatch(func);
    }

    public static <T,R> List<R> map(Collection<? extends T> list, Function<T,R> func) {
        return list.stream().map(func).collect(Collectors.toList());
    }

    public static <T,R> List<R> flatMap(Collection<? extends T> list, Function<T,Collection<R>> func) {
        return list.stream().flatMap(e -> func.apply(e).stream()).collect(Collectors.toList());
    }

    public static <T> List<T> convertList(Collection<?> srcList, Class<T> dstClass) {
        return srcList.stream().map(e -> (T)e).collect(Collectors.toList());
    }

    public static <T> List<T> extract(Collection<?> list, Class<T> targetClass) {
        return extract(list, targetClass, true);
    }

    public static <T> List<T> extract(Collection<?> list, Class<T> targetClass, boolean includeSubclass) {
        return list.stream()
            .filter(e ->
                   (targetClass.equals(e.getClass())
                    || (includeSubclass && targetClass.isInstance(e))))
            .map(e -> (T)e)
            .collect(Collectors.toList());
    }

}

/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;

public class ListBundle<K, V> {
    private Map<K, List<V>> listMap;

    public ListBundle() {
        listMap = new HashMap<>();
    }

    public ListBundle(Map<K, List<V>> map) {
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            List l = new ArrayList<>(entry.getValue());
            listMap.put(entry.getKey(), l);
        }
    }

    public void add(K tag, V element) {
        List<V> list = listMap.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            listMap.put(tag, list);
        }
        if (!list.contains(element))
            list.add(element);
    }

    public void remove(K tag, V element) {
        List<V> list = listMap.get(tag);
        if (list != null) {
            list.remove(element);
            if (list.size() == 0) {
                remove(tag);
            }
        }
    }

    public void remove(K tag) {
        listMap.remove(tag);
    }

    public void clear() {
        listMap.clear();
    }

    public List<V> getElements(K tag) {
        List<V> list = listMap.get(tag);
        if (list != null)
            return Collections.unmodifiableList(list);
        return null;
    }

    public List<V> getElementsAll() {
        List<V> list = new ArrayList<>();
        for (List<V> l : listMap.values()) {
            list.addAll(l);
        }
        return list;
    }

    public List<K> getTags() {
        return new ArrayList<>(listMap.keySet());
    }

    public int size() {
        return listMap.size();
    }
}


/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.lang.ref.*;
import java.util.*;
import java.util.function.*;

public class WeakValueHashMap<K,V> implements Map<K,V> {
    private Map<K, WeakReference<V>> map;
    private Map<WeakReference<V>, K> reverseMap;
    private ReferenceQueue<V> weakRefQueue;
    private List<Consumer<K>> referenceRemoveListenerList;

    public WeakValueHashMap() {
        map = new HashMap<>();
        reverseMap = new HashMap<>();
        weakRefQueue = new ReferenceQueue<>();
        referenceRemoveListenerList = null;
    }

    @Override
    public int size() {
        checkWeakReferenceReachability();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        checkWeakReferenceReachability();
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        checkWeakReferenceReachability();
        for (WeakReference<V> ref : map.values()) {
            if (ref.get() == value)
                return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        checkWeakReferenceReachability();
        WeakReference<V> ref = map.get(key);
        if (ref != null)
            return ref.get();
        return null;
    }

    @Override
    public V put(K key, V value) {
        checkWeakReferenceReachability();
        if (value == null)
            throw new IllegalArgumentException("Value must be null.");
        if (containsValue(value))
            throw new IllegalArgumentException("Value must be unique in collection.");
        WeakReference<V> ref;
        ref = map.get(key);
        V prev = null;
        if (ref != null) {
            prev = ref.get();
        }
        ref = new WeakReference(value, weakRefQueue);
        map.put(key, ref);
        reverseMap.put(ref, key);
        return prev;
    }

    @Override
    public V remove(Object key) {
        checkWeakReferenceReachability();
        WeakReference<V> ref = map.get(key);
        if (ref != null) {
            map.remove(key);
            reverseMap.remove(ref);
            return ref.get();
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        map.clear();
        reverseMap.clear();
        weakRefQueue = new ReferenceQueue<>();
    }

    // Returned object is not view, copy of current state.
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    // Returned object is not view, copy of current state.
    @Override
    public Collection<V> values() {
        checkWeakReferenceReachability();
        List<V> list = new ArrayList<>(size());
        for (WeakReference<V> ref : map.values()) {
            list.add(ref.get());
        }
        return list;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    private void checkWeakReferenceReachability() {
        WeakReference<V> ref;
        while ((ref = (WeakReference<V>)weakRefQueue.poll()) != null) {
            K key = reverseMap.get(ref);
            if (key != null) {
                map.remove(key);
                reverseMap.remove(ref);

                if (referenceRemoveListenerList != null) {
                    for (Consumer<K> l : referenceRemoveListenerList) {
                        l.accept(key);
                    }
                }
            }
        }
    }

    public void addReferenceRemoveListener(Consumer<K> listener) {
        if (referenceRemoveListenerList == null)
            referenceRemoveListenerList = new ArrayList<>();
        if (!referenceRemoveListenerList.contains(listener))
            referenceRemoveListenerList.add(listener);
    }

    public void removeReferenceRemoveListener(Consumer<K> listener) {
        referenceRemoveListenerList.remove(listener);
        if (referenceRemoveListenerList.size() == 0)
            referenceRemoveListenerList = null;
    }
}



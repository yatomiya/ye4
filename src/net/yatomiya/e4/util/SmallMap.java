/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;

// Best memory usage, but worst performance.
public class SmallMap<K, V> implements Map<K, V> {
    private Object[] kvs;

    public SmallMap() {
        kvs = null;
    }

    @Override
    public int size() {
        return kvs == null ? 0 : kvs.length / 2;
    }

    @Override
    public boolean isEmpty() {
        return kvs == null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (kvs != null) {
            for (int i = 0; i < kvs.length; i+= 2) {
                if (kvs[i] == key)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (kvs != null) {
            for (int i = 1; i < kvs.length; i+= 2) {
                if (kvs[i] == value)
                    return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key == null)
            throw new NullPointerException();

        if (kvs != null) {
            for (int i = 0; i < kvs.length; i += 2) {
                if (key.equals(kvs[i]))
                    return (V)kvs[i + 1];
            }
        }
        return null;
    }

    private void addCapacity(int add) {
        if (kvs == null) {
            kvs = new Object[add * 2];
        } else {
            Object[] old = kvs;
            kvs = new Object[old.length + add * 2];
            System.arraycopy(old, 0, kvs, 0, old.length);
        }
    }

    private void removeCapacity(int index, int length) {
        if (kvs.length - length * 2 <= 0) {
            kvs = null;
            return;
        }

        Object[] old = kvs;
        kvs = new Object[old.length - length * 2];
        if (index > 0)
            System.arraycopy(old, 0, kvs, 0, index);
        if (index < kvs.length)
            System.arraycopy(old, index + 2, kvs, index, old.length - index - length * 2);
    }

    private int indexOfKey(Object key) {
        int index = -1;
        if (kvs != null) {
            for (int i = 0; i < kvs.length; i += 2) {
                if (kvs[i].equals(key)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    @Override
    public V put(K key, V value) {
        if (key == null)
            throw new NullPointerException();

        int index = indexOfKey(key);
        Object oldValue = null;
        if (index < 0) {
            addCapacity(1);
            kvs[kvs.length - 2] = key;
            kvs[kvs.length - 1] = value;
        } else {
            oldValue = kvs[index + 1];
            kvs[index] = key;
            kvs[index + 1] = value;
        }

        return (V)oldValue;
    }

    @Override
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException();

        int index = indexOfKey(key);
        Object oldValue = null;
        if (index >= 0) {
            removeCapacity(index, 1);
        }

        return (V)oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        kvs = null;
    }

    @Override
    public Set<K> keySet() {
        if (isEmpty())
            return Collections.unmodifiableSet(Collections.EMPTY_SET);

        Set<K> set = new HashSet<K>(size());
        if (kvs != null) {
            for (int i = 0; i < kvs.length; i += 2) {
                set.add((K)kvs[i]);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Collection<V> values() {
        if (isEmpty())
            return Collections.unmodifiableList(Collections.EMPTY_LIST);

        List<V> list = new ArrayList<>(size());
        for (int i = 1; i < kvs.length; i += 2) {
            list.add((V)kvs[i]);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (isEmpty())
            return Collections.unmodifiableSet(Collections.EMPTY_SET);

        Set<Map.Entry<K, V>> set = new HashSet<>();
        for (int i = 0; i < kvs.length; i += 2) {
            set.add(new Entry((K)kvs[i], (V)kvs[i + 1]));
        }
        return Collections.unmodifiableSet(set);
    }

    private class Entry implements Map.Entry<K, V> {
        private K key;
        private V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

}




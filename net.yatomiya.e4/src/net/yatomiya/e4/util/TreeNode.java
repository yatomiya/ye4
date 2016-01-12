/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package net.yatomiya.e4.util;

import java.util.*;
import java.util.function.*;

public interface TreeNode<T extends TreeNode<T>> {
    T getParent();

    List<T> getChildren();

    default T getRoot() {
        T node = (T)this;
        while (node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }

    default int getDepth() {
        int depth = 0;
        T node = (T)this;
        while (node.getParent() != null) {
            node = node.getParent();
            depth++;
        }
        return depth;
    }

    default boolean isRoot() {
        return getParent() == null;
    }

    default boolean isAncestorOf(T node) {
        T parent = node.getParent();
        while (parent != null) {
            if (this == parent)
                return true;
            parent = parent.getParent();
        }
        return false;
    }

    default boolean isDescendantOf(T node) {
        return node.isAncestorOf((T)this);
    }

    default boolean visit(Predicate<T> visitor) {
        boolean result;
        result = visitor.test((T)this);
        if (result) {
            for (T child : getChildren()) {
                result = child.visit(visitor);
                if (!result)
                    break;
            }
        }
        return result;
    }

    default boolean visitAncestor(Predicate<T> visitor) {
        boolean result;
        result = visitor.test((T)this);
        if (result) {
            T parent = getParent();
            if (parent != null) {
                result = parent.visitAncestor(visitor);
            }
        }
        return result;
    }

    default List<T> findAll(Predicate<T> selector) {
        List<T> foundList = new ArrayList<>();
        visit(n -> {
            if (selector.test(n))
                foundList.add(n);
            return true;
        });
        return foundList;
    }

    default T find(Predicate<T> selector) {
        List<T> foundList = new ArrayList<>();
        visit(n -> {
            if (selector.test(n)) {
                foundList.add(n);
                return false;
            }
            return true;
        });
        if (foundList.size() > 0)
            return foundList.get(0);
        else
            return null;
    }

    default List<T> findAncestorAll(Predicate<T> selector) {
        List<T> foundList = new ArrayList<>();
        visitAncestor(n -> {
            if (selector.test(n))
                foundList.add(n);
            return true;
        });
        return foundList;
    }

    default T findAncestor(Predicate<T> selector) {
        List<T> foundList = new ArrayList<>();
        visitAncestor(n -> {
            if (selector.test(n)) {
                foundList.add(n);
                return false;
            }
            return true;
        });
        if (foundList.size() > 0)
            return foundList.get(0);
        else
            return null;
    }

    default List<T> listTree() {
        return findAll(n -> true);
    }

    default List<T> listAcnestor() {
        return findAncestorAll(n -> true);
    }

}

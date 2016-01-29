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

    default List<T> listTree() {
        return findTreeAll(n -> true);
    }

    default List<T> listAcnestor() {
        return findAncestorAll(n -> true);
    }

    default boolean visitTree(Predicate<T> visitor) {
        boolean result;
        result = visitor.test((T)this);
        if (result) {
            for (T child : getChildren()) {
                result = child.visitTree(visitor);
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

    default boolean visitUpstream(Predicate<T> visitor) {
        boolean result = true;

        result = visitor.test((T)this);

        if (result && getParent() != null) {
            int currentIndex = getParent().getChildren().indexOf(this);
            if (currentIndex == 0) {
                return getParent().visitUpstream(visitor);
            } else {
                return getParent().getChildren().get(currentIndex - 1).visitUpstream(visitor);
            }
        }
        return result;
    }

    default boolean visitUpward(Predicate<T> visitor) {
        boolean result = true;

        result = visitor.test((T)this);

        if (result && getParent() != null) {
            int currentIndex = getParent().getChildren().indexOf(this);
            if (currentIndex == 0) {
                return getParent().visitUpstream(visitor);
            } else {
                T node = getParent().getChildren().get(currentIndex - 1);
                while (node.getChildren().size() > 0) {
                    node = node.getChildren().get(node.getChildren().size() - 1);
                }
                return node.visitUpstream(visitor);
            }
        }
        return result;
    }

    default List<T> findAll(Function<Predicate<T>, Boolean> visitorFunc, Predicate<T> selector) {
        List<T> foundList = new ArrayList<>();
        visitorFunc.apply(n -> {
                if (selector.test(n))
                    foundList.add(n);
                return true;
        });
        return foundList;
    }

    default T find(Function<Predicate<T>, Boolean> visitorFunc, Predicate<T> selector) {
        Object[] found = new Object[1];
        visitorFunc.apply(n -> {
                if (selector.test(n)) {
                    found[0] = n;
                    return false;
                }
                return true;
        });
        return (T)found[0];
    }

    default List<T> findTreeAll(Predicate<T> selector) {
        return findAll(this::visitTree, selector);
    }

    default T findTree(Predicate<T> selector) {
        return find(this::visitTree, selector);
    }

    default List<T> findAncestorAll(Predicate<T> selector) {
        return findAll(this::visitAncestor, selector);
    }

    default T findAncestor(Predicate<T> selector) {
        return find(this::visitAncestor, selector);
    }

    default List<T> findUpstreamAll(Predicate<T> selector) {
        return findAll(this::visitUpstream, selector);
    }

    default T findUpstream(Predicate<T> selector) {
        return find(this::visitUpstream, selector);
    }

    default List<T> findUpwardAll(Predicate<T> selector) {
        return findAll(this::visitUpward, selector);
    }

    default T findUpward(Predicate<T> selector) {
        return find(this::visitUpward, selector);
    }
}

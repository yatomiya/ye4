/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.util.*;
import org.eclipse.jface.viewers.*;

public abstract class TypedViewerFilter extends ViewerFilter {
    private List<Object> typeList;

    public TypedViewerFilter() {
        super();

        typeList = new ArrayList<>();
    }

    public void addType(Object type) {
        if (!typeList.contains(type))
            typeList.add(type);
    }

    public void removeType(Object type) {
        typeList.remove(type);
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        for (Object type : typeList) {
            if (!selectElement(type, element))
                return false;
        }
        return true;
    }

    protected abstract boolean selectElement(Object type, Object element);
}




/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.util.*;
import org.eclipse.jface.viewers.*;

public class PostSelectionProvider implements IPostSelectionProvider {
    private List<ISelectionChangedListener> listenerList;

    private List<ISelectionChangedListener> postListenerList;

    private ISelection selection;

    public PostSelectionProvider() {
        listenerList = new ArrayList<ISelectionChangedListener>();
        postListenerList = new ArrayList<ISelectionChangedListener>();
        selection = StructuredSelection.EMPTY;
    }

    @Override
    public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
        if (!postListenerList.contains(listener)) {
            postListenerList.add(listener);
        }
    }

    @Override
    public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
        postListenerList.remove(listener);
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        listenerList.remove(listener);
    }

    @Override
    public ISelection getSelection() {
        return selection;
    }

    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;

        fireSelectionListener(new SelectionChangedEvent(this, selection));
    }

    public void setSelectionWithoutNotifyEvent(ISelection selection) {
        this.selection = selection;
    }

    private void fireSelectionListener(SelectionChangedEvent event) {
        for (ISelectionChangedListener l : listenerList) {
            l.selectionChanged(event);
        }
    }

}

/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.*;

public class LocalSelectionDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    private ISelectionProvider provider;

    public LocalSelectionDragAdapter(ISelectionProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public LocalSelectionTransfer getTransfer() {
        return LocalSelectionTransfer.getTransfer();
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        ISelection selection = provider.getSelection();
        if (!selection.isEmpty()) {
            getTransfer().setSelection(selection);
        } else {
            event.doit = false;
        }
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        ISelection selection = getTransfer().getSelection();
        event.data = selection;
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        getTransfer().setSelection(null);
    }

    public ISelectionProvider getSelectionProvider() {
        return provider;
    }
}

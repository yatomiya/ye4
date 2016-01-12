/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.ui.viewer.*;

public abstract class StructuredViewerDialog<T extends StructuredViewer> extends Dialog {
    private String title;
    private T viewer;
    private int style;
    private Object[] selectedElements;

    public StructuredViewerDialog(Shell parentShell, int style, String title) {
        super(parentShell);
        this.title = title;
        this.style = style;
        selectedElements = new Object[0];
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setEnabled(false);

        viewer = createViewer((Composite)getDialogArea(), style);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    selectedElements = SelectionUtils.toList(viewer.getSelection()).toArray();

                    getButton(IDialogConstants.OK_ID).setEnabled(selectedElements.length > 0);
                }
            });

        setupViewer(viewer);

        return c;
    }

    protected abstract T createViewer(Composite parent, int style);
    protected abstract void setupViewer(T viewer);

    public T getViewer() {
        return viewer;
    }

    public Object[] getSelectedElements() {
        return selectedElements.clone();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

}

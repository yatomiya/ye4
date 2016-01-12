/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;

public abstract class TreeViewerDialog extends StructuredViewerDialog<TreeViewer> {
    public TreeViewerDialog(Shell parentShell, int style, String title) {
        super(parentShell, style, title);
    }

    @Override
    protected TreeViewer createViewer(Composite parent, int style) {
        return new TreeViewer(parent, style);
    }
}


/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;




/**
 * ComboBoxCellEditor は横幅が固定文字数となっているところを、アイテムの最大幅にあわせるように変更。
 */
public class ShortComboBoxCellEditor extends ComboBoxCellEditor {
    public ShortComboBoxCellEditor(Composite parent, String[] items) {
        this(parent, items, SWT.NONE);
    }

    public ShortComboBoxCellEditor(Composite parent, String[] items, int style) {
        super(parent, items, style);
    }

    @Override
    public LayoutData getLayoutData() {
        LayoutData data = super.getLayoutData();
        CCombo box = (CCombo)getControl();
        if (box != null && !box.isDisposed()) {
            Point size = box.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            data.minimumWidth = size.x;
        }
        return data;
    }
}

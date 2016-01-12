/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.preference;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class LabelField extends FieldEditor {
    protected Label label;
    protected int style;

    public LabelField(String labelText, int style, Composite parent) {
        this.style = style;

        init("dummy", labelText);
        createControl(parent);
    }

    @Override
    public Label getLabelControl(Composite parent) {
        if (label == null) {
            label = new Label(parent, style);
            label.setFont(parent.getFont());
            String text = getLabelText();
            if (text != null) {
				label.setText(text);
			}
            label.addDisposeListener(event -> label = null);
        } else {
            checkParent(label, parent);
        }
        return label;
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData)label.getLayoutData();
        gd.horizontalSpan = numColumns;
    }

    @Override
    public int getNumberOfControls() {
        return 1;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = numColumns;
        label.setLayoutData(gd);
    }

    @Override
    protected void doLoad() {
        // do nothing
    }

    @Override
    protected void doLoadDefault() {
        // do nothing
    }

    @Override
    protected void doStore() {
        // do nothing
    }

    public static LabelField createSeparatorField(Composite parent) {
        return new LabelField("", SWT.SEPARATOR | SWT.HORIZONTAL, parent);
    }
}


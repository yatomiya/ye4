/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.preference;

import java.math.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.widgets.*;

public class SpinnerFieldEditor extends FieldEditor {
    private BigDecimalSpinner spinner;

    private int oldValue;


    public SpinnerFieldEditor(String name, String labelText, Composite parent,
                              int min, int max, int increment, int pageIncrement) {
        init(name, labelText);

        GridLayout layout = new GridLayout();
        layout.numColumns = getNumberOfControls();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = HORIZONTAL_GAP;
        parent.setLayout(layout);

        // create label control
        getLabelControl(parent);

        spinner = new BigDecimalSpinner(parent, SWT.NONE, 0, min, max, increment, pageIncrement);

        spinner.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    valueChanged();
                }
            });

        doFillIntoGrid(parent, layout.numColumns);
    }

    @Override
	protected void adjustForNumColumns(int numColumns) {
        ((GridData) spinner.getLayoutData()).horizontalSpan = numColumns - 1;
    }

    @Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        control.setLayoutData(gd);

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = true;
        spinner.setLayoutData(gd);
        spinner.layout();
    }

    @Override
	public int getNumberOfControls() {
        return 2;
    }

    @Override
	protected void doLoad() {
        int value = getPreferenceStore().getInt(getPreferenceName());
        spinner.setSelection(new BigDecimal(value));
        oldValue = value;
    }

    @Override
	protected void doLoadDefault() {
        int value = getPreferenceStore().getDefaultInt(getPreferenceName());
        spinner.setSelection(new BigDecimal(value));
        valueChanged();
    }

    @Override
	protected void doStore() {
        getPreferenceStore()
            .setValue(getPreferenceName(), spinner.getSelection().intValue());
    }

    public BigDecimalSpinner getSpinner() {
        return spinner;
    }

    @Override
	public void setFocus() {
        spinner.setFocus();
    }

    /**
     * Informs this field editor's listener, if it has one, about a change to
     * the value (<code>VALUE</code> property) provided that the old and new
     * values are different.
     * <p>
     * This hook is <em>not</em> called when the scale is initialized (or
     * reset to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() {
        setPresentsDefaultValue(false);

        int newValue = spinner.getSelection().intValue();
        if (newValue != oldValue) {
            fireStateChanged(IS_VALID, false, true);
            fireValueChanged(VALUE, new Integer(oldValue),
                    new Integer(newValue));
            oldValue = newValue;
        }
    }

}



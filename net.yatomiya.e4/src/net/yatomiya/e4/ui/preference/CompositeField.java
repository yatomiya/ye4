/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.preference;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.util.*;

public class CompositeField extends FieldEditor {
    protected StructuredFieldEditorPreferencePage page;
    protected Composite parentComposite;
    protected int style;
    protected Composite control;
    protected List<FieldEditor> fields;

    public CompositeField(StructuredFieldEditorPreferencePage page, Composite parentComposite, int style) {
        this(page, parentComposite, style, null);
    }

    public CompositeField(StructuredFieldEditorPreferencePage page, Composite parentComposite, int style, String label) {
        init("", label == null ? "" : label);
        this.page = page;
        this.style = style;
        this.parentComposite = parentComposite;
        fields = new ArrayList<>();
        createControl(parentComposite);
    }

    public void addField(FieldEditor field) {
        fields.add(field);

        page.addFieldFromCompositeField(field);
    }

    public List<FieldEditor> getFieldEditors() {
        return Collections.unmodifiableList(fields);
    }

    public Composite getControl() {
        return control;
    }

    protected void adjustLayout() {
        if ((style & SWT.HORIZONTAL) != 0) {
            int numColumns = 0;
            for (FieldEditor f : fields) {
                numColumns += f.getNumberOfControls();
            }
            ((GridLayout)control.getLayout()).numColumns = numColumns;

            for (FieldEditor f : fields) {
                if (f instanceof CompositeField) {
                    ((CompositeField)f).adjustLayout();
                }
            }
        } else {
            int numColumns = 0;
            for (FieldEditor f : fields) {
                numColumns = Math.max(f.getNumberOfControls(), numColumns);
            }
            ((GridLayout)control.getLayout()).numColumns = numColumns;

            for (FieldEditor f : fields) {
//            f.adjustForNumColumns(numColumns);
                {
                    Method m = JUtils.findMethod(f.getClass(), "adjustForNumColumns", int.class);
                    m.setAccessible(true);
                    try {
                        m.invoke(f, new Integer(numColumns));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }

                if (f instanceof CompositeField) {
                    ((CompositeField)f).adjustLayout();
                }
            }
        }
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData)control.getLayoutData();
        gd.horizontalSpan = numColumns;
        if (!JUtils.isEmpty(getLabelText()))
            gd.horizontalSpan -= 1;
    }

    @Override
    public int getNumberOfControls() {
        if (!JUtils.isEmpty(getLabelText()))
            return 2;
        else
            return 1;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        if (!JUtils.isEmpty(getLabelText())) {
            getLabelControl(parent);
        }

        control = createCompositeControl(parent, style);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = numColumns;
        if (!JUtils.isEmpty(getLabelText()))
            gd.horizontalSpan -= 1;
        control.setLayoutData(gd);

        GridLayout layout = new GridLayout();
        control.setLayout(layout);
    }

    protected Composite createCompositeControl(Composite parent, int style) {
        return new Composite(parent, SWT.NONE);
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

    @Override
    public void setEnabled(boolean enabled, Composite parent) {
        setEnable(enabled);
    }

    public void setEnable(boolean enabled) {
        if (getLabelControl() != null)
            getLabelControl().setEnabled(enabled);

        UIUtils.recursiveSetEnabled(control, enabled);
    }
}

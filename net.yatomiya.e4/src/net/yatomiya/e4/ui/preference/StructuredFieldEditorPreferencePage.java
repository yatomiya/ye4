/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.preference;

import java.util.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import net.yatomiya.e4.util.*;

public abstract class StructuredFieldEditorPreferencePage extends FieldEditorPreferencePage {
    protected List<FieldEditor> fields;
    protected CompositeField rootCompositeField;

    protected StructuredFieldEditorPreferencePage(String title) {
        super(title, GRID);

        fields = new ArrayList<>();
    }

    @Override
    public void addField(FieldEditor field) {
        throw new UnsupportedOperationException();
    }

    protected void addFieldFromCompositeField(FieldEditor field) {
        super.addField(field);

        fields.add(field);
    }

    public FieldEditor getField(String name) {
        return CUtils.findFirst(fields, f -> f.getPreferenceName().equals(name));
    }

    public CompositeField getRootCompositeField() {
        if (rootCompositeField == null) {
            rootCompositeField = new CompositeField(this, getFieldEditorParent(), SWT.NONE);
        }
        return rootCompositeField;
    }

    @Override
    protected void adjustGridLayout() {
        if (rootCompositeField == null)
            return;
        rootCompositeField.adjustLayout();
    }
}



















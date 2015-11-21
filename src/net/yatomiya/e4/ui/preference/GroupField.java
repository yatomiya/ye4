/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.preference;

import org.eclipse.swt.widgets.*;

public class GroupField extends CompositeField {

    public GroupField(StructuredFieldEditorPreferencePage page, Composite parentComposite, int style, String groupText) {
        super(page, parentComposite, style);

        getControl().setText(groupText);
    }

    @Override
    public Group getControl() {
        return (Group)super.getControl();
    }

    @Override
    protected Composite createCompositeControl(Composite parent, int style) {
        Group group = new Group(parent, style);
        return group;
    }
}

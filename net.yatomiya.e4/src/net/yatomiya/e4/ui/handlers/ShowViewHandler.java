/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.e4.ui.model.application.descriptor.basic.*;
import org.eclipse.jface.window.*;
import net.yatomiya.e4.services.part.*;
import net.yatomiya.e4.ui.dialogs.*;
import net.yatomiya.e4.util.*;

public class ShowViewHandler {
    @Execute
    public void execute() {
        ShowViewDialog dlg = createShowViewDialog();
        int result = dlg.open();
        if (result == Window.OK) {
            MPartDescriptor[] selection = CUtils.convertArray(dlg.getSelectedElements(), MPartDescriptor.class);
            if (selection.length > 0) {
                doOkPressed(selection);
            }
        }
    }

    protected ShowViewDialog createShowViewDialog() {
        return new ShowViewDialog(null);
    }

    protected void doOkPressed(MPartDescriptor[] selection) {
        for (MPartDescriptor d : selection) {
            EUtils.get(PartService.class).openPart(d.getElementId(), true);
        }
    }
}


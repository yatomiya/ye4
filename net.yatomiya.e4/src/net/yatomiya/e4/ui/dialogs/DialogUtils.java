/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;

public class DialogUtils {

    public static void openMessage(Shell parentShell, String title, String message) {
        MessageDialog.openInformation(parentShell, title, message);
    }

    public static void openError(Shell parentShell, String title, String message) {
        MessageDialog.openError(parentShell, title, message);
    }

    public static void openWarning(Shell parentShell, String title, String message) {
        MessageDialog.openWarning(parentShell, title, message);
    }

    public static boolean openConfirmation(Shell parentShell, String title, String message) {
        return MessageDialog.openConfirm(parentShell, title, message);
    }

    public static boolean openQuestion(Shell parentShell, String title, String message) {
        return MessageDialog.openQuestion(parentShell, title, message);
    }

/*
    public static int openDialog(Shell parentShell, String title, String message, int style) {
        if (parentShell == null)
            parentShell = Display.getDefault().getActiveShell();

        MessageBox d = new MessageBox(parentShell, style | SWT.APPLICATION_MODAL);
        d.setText(title);
        d.setMessage(message);
        return d.open();
    }

    public static void openMessage(Shell parentShell, String title, String message) {
        openDialog(parentShell, title, message, SWT.NONE);
    }

    public static void openError(Shell parentShell, String title, String message) {
        openDialog(parentShell, title, message, SWT.ICON_ERROR);
    }

    public static void openWarning(Shell parentShell, String title, String message) {
        openDialog(parentShell, title, message, SWT.ICON_WARNING);
    }

    public static boolean openConfirmation(Shell parentShell, String title, String message) {
        return openDialog(parentShell, title, message, SWT.OK | SWT.CANCEL) == SWT.OK;
    }

    public static boolean openQuestion(Shell parentShell, String title, String message) {
        return openDialog(parentShell, title, message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.YES;
    }
*/
}


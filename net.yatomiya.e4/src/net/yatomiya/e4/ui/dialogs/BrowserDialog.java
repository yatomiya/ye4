/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;


/*
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class BrowserDialog extends MessageDialog {
    protected Browser browser;
    protected String html;

    public BrowserDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType,
                         String[] dialogButtonLabels, int defaultIndex, String html) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);

        this.html = html;
    }

    @Override
    protected Control createCustomArea(Composite parent) {
        browser = new Browser(parent, SWT.NONE);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.setJavascriptEnabled(false);
        if (html != null) {
            browser.setText(html);
        }

        return browser;
    }

    public Browser getBrowser() {
        return browser;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

}
*/


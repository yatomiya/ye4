/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;



public abstract class MessageAndButtonPanel extends Composite {
    protected Composite contentPane;

    protected Composite buttonPane;
    protected Label messageLabel;
    protected Button[] buttons;

    public MessageAndButtonPanel(Composite parent, int style) {
        super(parent, style);

        setLayout(new LinearLayout(SWT.VERTICAL));

        contentPane = new Composite(this, SWT.NONE);
        contentPane.setLayoutData(new LinearData(SWT.FILL, SWT.FILL, true, true));
        contentPane.setLayout(new FillLayout());

        buttonPane = new Composite(this, SWT.NONE);
        buttonPane.setLayoutData(new LinearData(SWT.FILL, SWT.CENTER, true, false));
        buttonPane.setLayout(new LinearLayout());

        messageLabel = new Label(buttonPane, SWT.NONE);
        messageLabel.setLayoutData(new LinearData(SWT.FILL, SWT.CENTER, true, false));

        buttons = createButtons(buttonPane, getButtonLabels());

        createContents(contentPane);
    }

    protected abstract void createContents(Composite pane);

    protected Button[] createButtons(Composite pane, String[] buttonLabels) {
        Button[] buttons = new Button[buttonLabels.length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(pane, SWT.PUSH);
            buttons[i].setText(buttonLabels[i]);
        }

        return buttons;
    }

    protected String[] getButtonLabels() {
        return new String[] { "OK", "キャンセル" };
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public String getMessage() {
        return messageLabel.getText();
    }
}


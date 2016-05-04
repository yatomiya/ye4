/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.widgets.*;


/**
 * alternative to jface.MessageDialog, which use Text widget for message body (MessageDialog uses Label widget).
 */
public class TextMessageDialog extends Dialog {
    public static enum Type {
        NONE,
        ERROR,
        INFORMATION,
        QUESTION,
        WARNING,
        CONFIRM,
        QUESTION_WITH_CANCEL
    };

    protected Type type;
    protected String title;
    protected String message;
    protected int textStyle;
    protected String[] buttonLabels;
    protected int defaultButtonIndex;

    protected Image iconImage;
    protected Label iconLabel;
    protected Text textControl;
    protected Button[] buttons;

    public TextMessageDialog(Shell parentShell, Type type, String title, String message, int textStyle,
                             String[] buttonLabels, int defaultButtonIndex) {
        super(parentShell);

        this.type = type;
        this.title = title;
        this.message = message;
        this.textStyle = textStyle;
        this.buttonLabels = buttonLabels;
        this.defaultButtonIndex = defaultButtonIndex;

        iconImage = getIconImage(type);
    }

    public TextMessageDialog(Shell parentShell, Type type, String title, String message, int textStyle) {
        this(parentShell, type, title, message, textStyle, getButtonLabels(type), 0);
    }

	@Override
    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
    }

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }

    @Override
	protected void createButtonsForButtonBar(Composite parent) {
        buttons = new Button[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            Button button = createButton(parent, i, label,
                                         defaultButtonIndex == i);
            buttons[i] = button;
        }
    }

    @Override
	protected Control createDialogArea(Composite parent) {
        Composite base = new Composite(parent, SWT.NONE);

        base.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        base.setLayout(new LinearLayout());

        if (iconImage != null) {
            iconLabel = new Label(base, SWT.NONE);
            iconLabel.setLayoutData(new LinearData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            iconLabel.setImage(iconImage);
        }

        textControl = new Text(base,textStyle);
        textControl.setLayoutData(new LinearData(SWT.FILL, SWT.FILL, true, true));
        textControl.setText(message);

        return base;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

	static String[] getButtonLabels(Type type) {
		String[] dialogButtonLabels;
		switch (type) {
        case NONE:
		case ERROR:
		case INFORMATION:
		case WARNING: {
			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };
			break;
		}
		case CONFIRM: {
			dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL,
					IDialogConstants.CANCEL_LABEL };
			break;
		}
		case QUESTION: {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
			break;
		}
		case QUESTION_WITH_CANCEL: {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
                    IDialogConstants.NO_LABEL,
                    IDialogConstants.CANCEL_LABEL };
			break;
		}
		default: {
			throw new IllegalArgumentException(
					"Illegal value for kind in NMessageDialog.open()"); //$NON-NLS-1$
		}
		}
		return dialogButtonLabels;
	}

    Image getIconImage(Type type) {
        Shell shell = getShell();
		if (shell == null || shell.isDisposed()) {
			shell = getParentShell();
		}
		Display display;
        if (shell == null)
            display = Display.getCurrent();
        else
            display = shell.getDisplay();

        Image image = null;
        switch (type) {
        case NONE:
            break;
        case ERROR:
            image = display.getSystemImage(SWT.ICON_ERROR);
            break;
        case INFORMATION:
            image = display.getSystemImage(SWT.ICON_INFORMATION);
            break;
        case WARNING:
            image = display.getSystemImage(SWT.ICON_WARNING);
            break;
        case QUESTION:
        case CONFIRM:
        case QUESTION_WITH_CANCEL:
            image = display.getSystemImage(SWT.ICON_QUESTION);
            break;
        }

        return image;
    }

    public static TextMessageDialog create(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.NONE, title, message, textStyle, getButtonLabels(Type.NONE), 0);
    }

    public static TextMessageDialog createError(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.ERROR, title, message, textStyle, getButtonLabels(Type.ERROR), 0);
    }

    public static TextMessageDialog createInformation(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.INFORMATION, title, message, textStyle, getButtonLabels(Type.INFORMATION), 0);
    }

    public static TextMessageDialog createQuestion(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.QUESTION, title, message, textStyle, getButtonLabels(Type.QUESTION), 0);
    }

    public static TextMessageDialog createWarning(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.WARNING, title, message, textStyle, getButtonLabels(Type.WARNING), 0);
    }

    public static TextMessageDialog createConfirm(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.CONFIRM, title, message, textStyle, getButtonLabels(Type.CONFIRM), 0);
    }

    public static TextMessageDialog createQuestionWithCancel(Shell parentShell, String title, String message, int textStyle) {
        return new TextMessageDialog(parentShell, Type.QUESTION_WITH_CANCEL, title, message, textStyle, getButtonLabels(Type.QUESTION_WITH_CANCEL), 0);
    }

}



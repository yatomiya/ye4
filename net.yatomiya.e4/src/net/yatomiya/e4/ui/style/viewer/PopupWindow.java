/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.style.*;

public abstract class PopupWindow {
    private PopupWindowManager parentManager;
    private StyleNode activateNode;
    private StyleViewer parentViewer;
    private Shell shell;
    private boolean isCursorInShell;

    public PopupWindow(PopupWindowManager parentManager, StyleNode activateNode) {
        this.parentManager = parentManager;
        this.activateNode = activateNode;

        parentViewer = parentManager.getViewer();

        isCursorInShell = false;
    }

    void create() {
        // Dont use SWT.BORDER. In BORDER area, mouse event is not notified.
        shell = new Shell(parentViewer.getTextWidget().getShell(), SWT.NO_TRIM | SWT.MODELESS);

        FillLayout layout = new FillLayout();
        // margin for drawing frame line.
        layout.marginWidth = 1;
        layout.marginHeight = 1;
        shell.setLayout(layout);
        shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        createContents(shell);

        installListeners();
    }

    void installListeners() {
        final Display display = shell.getDisplay();

        final Listener boundsCheckListener = new Listener() {
                @Override
                public void handleEvent(Event event) {

                    switch (event.type) {
                    case SWT.MouseEnter:
                    case SWT.MouseExit:
                    case SWT.MouseMove:
                        boolean oldSticky = isSticky();

                        checkCursorInShell();

                        if (!oldSticky && isSticky()) {
                            mouseEnteredInShell();
                        }

                        if (oldSticky && !isSticky()) {
                            shell.close();
                        }
                    }
                }
            };
        display.addFilter(SWT.MouseMove, boundsCheckListener);
        display.addFilter(SWT.MouseExit, boundsCheckListener);
        shell.addDisposeListener(event -> {
                display.removeFilter(SWT.MouseMove, boundsCheckListener);
                display.removeFilter(SWT.MouseExit, boundsCheckListener);
            });
    }

    protected abstract void createContents(Shell shell);

    public Shell getShell() {
        return shell;
    }

    public PopupWindowManager getParentManager() {
        return parentManager;
    }

    public StyleNode getActivateNode() {
        return activateNode;
    }

    public boolean isSticky() {
        return isCursorInShell;
    }

    public boolean checkCursorInShell() {
        Point loc = shell.getDisplay().getCursorLocation();
        Rectangle bounds = shell.getBounds();
        isCursorInShell = bounds.contains(loc);
        return isCursorInShell;
    }

    protected void mouseEnteredInShell() {
        getShell().setFocus();
    }

}


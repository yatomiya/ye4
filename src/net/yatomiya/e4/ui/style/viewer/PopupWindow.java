/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.ui.util.*;

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
        shell.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent event) {
                    display.removeFilter(SWT.MouseMove, boundsCheckListener);
                    display.removeFilter(SWT.MouseExit, boundsCheckListener);
                }
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

    public static Rectangle calcPopupBounds(Point cursorLoc, Point size, Rectangle tokenRect) {
        Rectangle rect = new Rectangle(0, 0, size.x, size.y);
        Monitor monitor = UIUtils.getMonitorAt(cursorLoc.x, cursorLoc.y);
        Rectangle tRect;
        if (tokenRect != null) {
            tRect = new Rectangle(tokenRect.x, tokenRect.y, tokenRect.width, tokenRect.height);
        } else {
            tRect = new Rectangle(cursorLoc.x, cursorLoc.y, 1, 1);
        }
        Rectangle monitorArea = monitor.getClientArea();
        Rectangle upperArea = new Rectangle(monitorArea.x, monitorArea.y, monitorArea.width, tRect.y - monitorArea.y);
        Rectangle lowerArea = new Rectangle(monitorArea.x, tRect.y + tRect.height, monitorArea.width,
                                            monitorArea.y + monitorArea.height - (tRect.y + tRect.height));

        if (rect.height <= upperArea.height) {
            rect.y = tRect.y - rect.height;
        } else if (rect.height <= lowerArea.height) {
            rect.y = tRect.y + tRect.height;
        } else {
            rect.y = monitorArea.y;
            if (rect.height > monitorArea.height)
                rect.height = monitorArea.height;
        }

        int rightWidth = monitorArea.x + monitorArea.width - tRect.x;
        int leftWidth = monitorArea.width - rightWidth;
        if (rect.width <= rightWidth) {
            rect.x = tRect.x;
        } else if (rect.width <= monitorArea.width) {
            rect.x = monitorArea.x + monitorArea.width - rect.width;
        } else {
            rect.x = monitorArea.x;
            rect.width = monitorArea.width;
        }

        return rect;
    }

    public static Rectangle calcHorizontalPopupBounds(Point cursorLoc, Point size, Rectangle tokenRect) {
        Rectangle rect = new Rectangle(0, 0, size.x, size.y);
        Monitor monitor = UIUtils.getMonitorAt(cursorLoc.x, cursorLoc.y);
        Rectangle tRect;

        int diffWidth = 16;
        int diffHeight = 4;
        tRect = new Rectangle(cursorLoc.x - diffWidth / 2, cursorLoc.y - diffHeight / 2, diffWidth, diffHeight);
/*
        if (tokenRect != null) {
            tRect = new Rectangle(tokenRect.x, tokenRect.y, tokenRect.width, tokenRect.height);
        } else {
            tRect = new Rectangle(cursorLoc.x, cursorLoc.y, 1, 1);
        }
*/

        Rectangle monitorArea = monitor.getClientArea();
        Rectangle rightArea = new Rectangle(tRect.x + tRect.width, monitorArea.y,
                                            (monitorArea.x + monitorArea.width) - (tRect.x + tRect.width), monitorArea.height);
        Rectangle leftArea = new Rectangle(monitorArea.x, monitorArea.y, tRect.x - monitorArea.x, monitorArea.height);

        if (rect.width <= rightArea.width) {
            rect.x = rightArea.x;
        } else if (rect.width <= leftArea.width) {
            rect.x = tRect.x - rect.width;
        } else {
            rect.x = rightArea.x + rightArea.width - rect.width;
        }

        rect.y = (tRect.y + tRect.height/2) - rect.height/2;
        if (rect.y < monitorArea.y) {
            rect.y = monitorArea.y;
        }
        if (rect.y + rect.height > monitorArea.y + monitorArea.height) {
            rect.height = monitorArea.height;
        }

        return rect;
    }



}

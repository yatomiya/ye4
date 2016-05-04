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
    private boolean isSticky;
    private boolean isMouseInShellOnce;

    public PopupWindow(PopupWindowManager parentManager, StyleNode activateNode) {
        this.parentManager = parentManager;
        this.activateNode = activateNode;

        parentViewer = parentManager.getViewer();
        isSticky = false;
        isMouseInShellOnce = false;
    }

    public void create() {
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

    public void installListeners() {
        final Display display = shell.getDisplay();

        /**
         * シェルの領域にマウスが入ったかどうかをチェックする。
         * Shell.addMouseXXX() では、シェルの内部のウィジェットにマウスが入ったときのイベントをキャッチできないので、
         * addFilter()を使う。
         */
        class ShellBoundsCheckListener implements Listener {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.MouseEnter:
                case SWT.MouseExit:
                case SWT.MouseMove:
                    checkFocus();
                    checkBoundsClose();
                }
            }
        }
        final Listener boundsCheckListener = new ShellBoundsCheckListener();

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

    public void setSticky(boolean v) {
        boolean changed = v != isSticky;
        isSticky = v;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public boolean checkCursorInShell() {
        Point loc = shell.getDisplay().getCursorLocation();
        Rectangle bounds = shell.getBounds();
        return bounds.contains(loc);
    }

    protected void checkFocus() {
        Display display = shell.getDisplay();
        boolean inShell = checkCursorInShell();

        if (inShell) {
            Shell focusShell = display.getFocusControl().getShell();
            if (shell != focusShell) {
                shell.setFocus();
            }
        }
    }

    protected void checkBoundsClose() {
        Display display = shell.getDisplay();
        boolean inShell = checkCursorInShell();

        if (inShell)
            isMouseInShellOnce = true;
        if (!inShell) {
            if (isMouseInShellOnce) {
                if (!isSticky()) {
                    /**
                     * コンテキストメニューをクリックしたときに、カーソルが PopupWindow の外にあると
                     * ここの処理で PopupWindow が dispose される。メニューに e4 アプリケーションモデルのハンドラーを
                     * 使っていると、ハンドラーが execute される前に dispose イベントでハンドラーが破棄されてしまい、
                     * execute が呼び出されない。
                     * それを避けるために、 execute が呼び出される余裕を asyncExec() で作る。
                     */
                    display.asyncExec(() -> {
                            parentManager.deactivatePopupWindow();
                        });
                }
            }
        }
    }
}


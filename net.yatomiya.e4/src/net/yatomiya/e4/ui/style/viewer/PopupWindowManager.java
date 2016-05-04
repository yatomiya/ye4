/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.ui.util.*;

public class PopupWindowManager {
    StyleViewer viewer;
    StyleNode activationNode;
    Rectangle activationRect;
    MouseEvent openMouseEvent;
    PopupWindow popupWindow;
    MouseMoveListener activeRectChecker;
    StyleMouseListener nodeListener;

    public PopupWindowManager(StyleViewer viewer) {
        this.viewer = viewer;

        nodeListener = new StyleMouseAdapter() {
            };
        viewer.getMouseManager().addListener(nodeListener);

        StyledText st = viewer.getTextWidget();

        activeRectChecker = new MouseMoveListener() {
                @Override
                public void mouseMove(MouseEvent event) {
                    if (isPopupWindowActive()) {
                        if (!activationRect.contains(event.x, event.y))
                            checkPopupWindowDeactivation();
                    }
                }
            };
        st.addMouseMoveListener(activeRectChecker);
    }

    void dispose() {
        deactivatePopupWindow();

        viewer.getMouseManager().removeListener(nodeListener);

        StyledText st = viewer.getTextWidget();
        if (!UIUtils.isDisposed(st))
            st.removeMouseMoveListener(activeRectChecker);

        activationNode = null;
        activationRect = null;
        popupWindow = null;
        activeRectChecker = null;
    }

    public StyleViewer getViewer() {
        return viewer;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public boolean isPopupWindowActive() {
        return popupWindow != null;
    }

    public StyleNode getActivationStyleNode() {
        return activationNode;
    }

    public Rectangle getActivationRect() {
        return activationRect;
    }

    public MouseEvent getOpenMouseEvent() {
        return openMouseEvent;
    }

    public void open(PopupWindow popupWindow, StyleNode activeNode, MouseEvent openMouseEvent) {
        if (isPopupWindowActive())
            return;

        this.popupWindow = popupWindow;
        this.activationNode = activeNode;
        this.openMouseEvent = openMouseEvent;

        StyleViewer viewer = getViewer();
        StyledText st = viewer.getTextWidget();
        if (UIUtils.isDisposed(st))
            return;

        Rectangle clientArea = st.getClientArea();
        Rectangle tokenRect = st.getTextBounds(activeNode.getOffset(), activeNode.getEnd());
        activationRect = new Rectangle(tokenRect.x, tokenRect.y, tokenRect.width, tokenRect.height);
        activationRect.intersects(clientArea);

        /**
         * region の範囲判定は getOffsetAtLocation() を使う。しかし、 getTextBounds() で取得した範囲は、 getOffsetAtLocation() で
         * 反応する範囲よりも小さくなる（みたい）。多分、 getTextBounds() はフォントのサイズも考慮したサイズであり、升目の
         * 範囲よりも小さくなっているのではないか。 region からカーソルが離れる判定に使う activeRect は getTextBounds() を使用している。
         * その結果、 region に入ったカーソルの位置が activeRect からすでに離れている、という判定をされてしまう。
         * しかし、 getOffsetAtLocation() で判定に使われているはずの範囲を取得するメソッドがない（見つからない）。
         * 仕方ないので、この時点でのカーソルの位置を無理やり activeRect に含める。
         */
        Point cursorPos = st.toControl(st.getDisplay().getCursorLocation());
        Rectangle cursorArea = new Rectangle(cursorPos.x, cursorPos.y, 1, 1);
        activationRect = activationRect.union(cursorArea);

        popupWindow.create();

        popupWindow.getShell().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent event) {
                    deactivatePopupWindow();
                }
            });

        popupWindow.getShell().setVisible(true);
    }

    public void deactivatePopupWindow() {
        if (!isPopupWindowActive())
            return;

        Shell shell = popupWindow.getShell();
        if (!UIUtils.isDisposed(shell)) {
            StyledText st = viewer.getTextWidget();
            if (!UIUtils.isDisposed(st)) {
                if (UIUtils.isAncesterOf(shell, shell.getDisplay().getFocusControl())) {
                    st.setFocus();
                }
            }
            shell.close();
        }

        popupWindow = null;
        activationNode = null;
        openMouseEvent = null;
    }

    public void checkPopupWindowDeactivation() {
        if (isPopupWindowActive()) {
            if (!popupWindow.isSticky()) {
                deactivatePopupWindow();
            }
        }
    }
}



/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import java.util.*;
import java.util.List;
import org.eclipse.jface.text.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.util.*;

public class StyleMouseManager {
    private StyleViewer viewer;
    private InternalListener iListener;
    private int hoverDelayTime = 1000/10;
    private List<StyleMouseListener> listeners;

    StyleMouseManager(StyleViewer viewer) {
        this.viewer = viewer;
        StyledText st = viewer.getTextWidget();

        iListener = new InternalListener();
        st.addMouseListener(iListener);
        st.addMouseTrackListener(iListener);
        st.addMouseMoveListener(iListener);
        st.addFocusListener(iListener);
        st.addMenuDetectListener(iListener);
        viewer.addTextListener(iListener);

        listeners = new ArrayList<>();
    }

    public void dispose() {
        StyledText st = viewer.getTextWidget();
        if (UIUtils.isDisposed(st))
            return;

        st.removeMouseListener(iListener);
        st.removeMouseTrackListener(iListener);
        st.removeMouseMoveListener(iListener);
        st.removeFocusListener(iListener);
        st.removeMenuDetectListener(iListener);
        viewer.removeTextListener(iListener);

        viewer = null;
        listeners = null;
    }

    public StyleViewer getViewer() {
        return viewer;
    }

    public void addListener(StyleMouseListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(StyleMouseListener listener) {
        listeners.remove(listener);
    }

    protected void onEnter(StyleViewer viewer, StyleNode node, MouseEvent event) {
        for (StyleMouseListener l : listeners)
            l.onEnter(viewer, node, event);
    }

    protected void onExit(StyleViewer viewer, StyleNode node) {
        for (StyleMouseListener l : listeners)
            l.onExit(viewer, node);
    }

    protected void onMove(StyleViewer viewer, StyleNode node, MouseEvent event) {
        for (StyleMouseListener l : listeners)
            l.onMove(viewer, node, event);
    }

    protected void onClick(StyleViewer viewer, StyleNode node, MouseEvent event) {
        for (StyleMouseListener l : listeners)
            l.onClick(viewer, node, event);
    }

    protected void onHover(StyleViewer viewer, StyleNode node, MouseEvent event) {
        for (StyleMouseListener l : listeners)
            l.onHover(viewer, node, event);
    }

    protected void onContextMenu(StyleViewer viewer, StyleNode node, MenuDetectEvent event) {
        for (StyleMouseListener l : listeners)
            l.onContextMenu(viewer, node, event);
    }

    public StyleNode getCurrentNodeOnCursor() {
        return iListener.currentNodeOnCursor;
    }

    public StyleNode getSelectedContextMenuNode() {
        return iListener.contextMenuNode;
    }

    public int getHoverDelayTime() {
        return hoverDelayTime;
    }

    public void setHoverDelayTime(int v) {
        hoverDelayTime = v;
    }

    public boolean isSelectionDragging() {
        return iListener.isSelectionDragging();
    }

    class InternalListener implements MouseListener, MouseMoveListener, MouseTrackListener, FocusListener, MenuDetectListener, ITextListener {
        StyleNode currentNodeOnCursor;
        class ClickState {
            int button;
            Point loc;
            StyleNode node;
        }
        ClickState clickState;
        DelayedHoverChecker checker;
        StyleNode contextMenuNode;

        InternalListener() {
            clickState = null;
            checker = null;

            clearState();
        }

        void clearState() {
            if (currentNodeOnCursor != null) {
                onExit(viewer, currentNodeOnCursor);
            }

            currentNodeOnCursor = null;
            clickState = null;
            cancelDelayedHoverChecker();
        }

        boolean isSelectionDragging() {
            return clickState != null
                && clickState.button == 1;
        }

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (isSelectionDragging())
                return;

            clearState();
        }

        @Override
        public void mouseEnter(MouseEvent e) {
            if (isSelectionDragging())
                return;

            clearState();
            mouseMove(e);
        }

        @Override
        public void mouseExit(MouseEvent e) {
            if (isSelectionDragging())
                return;

            clearState();
        }

        @Override
        public void mouseHover(MouseEvent e) {
            // swt hover event is not used.
        }

        @Override
        public void mouseMove(MouseEvent e) {
            StyledText st = viewer.getTextWidget();

            if (isSelectionDragging())
                return;

            if (st.getDisplay().getActiveShell() != st.getShell()) {
                clearState();
                return;
            }

            StyleNode node = null;

            int offset = UIUtils.getOffsetForCursorLocation(viewer.getTextWidget());
            if (offset >= 0) {
                if (viewer.getDocument() != null)
                    node = viewer.getDocument().getStyleNode(offset);
            }

            // When entering node
            if (node != currentNodeOnCursor) {
                clearState();

                // If two nodes are adjacent, exiting from last region and entering new region
                // occurs at once.

                if (node != null) {
                    checker = new DelayedHoverChecker();
                    checker.mouseEvent = e;
                    checker.targetNode = node;

                    EUtils.timerExec(hoverDelayTime, checker);

                    onEnter(viewer, node, e);
                }

                currentNodeOnCursor = node;
            }

            if (node != null)
                onMove(viewer, node, e);
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (clickState == null) {
                clickState = new ClickState();
                clickState.button = e.button;
                clickState.loc = new Point(e.x, e.y);
                clickState.node = currentNodeOnCursor;
            }
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (clickState != null
                && clickState.button == 1
                && clickState.button == e.button
                && clickState.loc.equals(new Point(e.x, e.y))
                && clickState.node != null) {
                onClick(viewer, clickState.node, e);
            }
            clickState = null;
        }

        void cancelDelayedHoverChecker() {
            if (checker != null) {
                checker.doCancel = true;
                checker = null;
            }
        }

        @Override
        public void menuDetected(MenuDetectEvent e) {
            contextMenuNode = currentNodeOnCursor;

            onContextMenu(viewer, contextMenuNode, e);
        }

        class DelayedHoverChecker implements Runnable {
            boolean doCancel = false;
            MouseEvent mouseEvent;
            StyleNode targetNode;

            @Override
            public void run() {
                if (doCancel)
                    return;

                if (!UIUtils.isDisposed(viewer.getTextWidget()))
                    onHover(viewer, targetNode, mouseEvent);
            }
        }

        @Override
        public void textChanged(TextEvent e) {
            StyledText st = viewer.getTextWidget();
            if (!UIUtils.isDisposed(st)) {
                Display display = st.getDisplay();
                if (display.getFocusControl() == st
                    && display.getCursorControl() == st) {
                    Point cursorPos = display.getCursorLocation();
                    cursorPos = display.map(null, st, cursorPos);
                    Event event = new Event();
                    event.widget = st;
                    event.x = cursorPos.x;
                    event.y = cursorPos.y;
                    MouseEvent me = new MouseEvent(event);
                    mouseMove(me);
                }
            }
        }
    }
}


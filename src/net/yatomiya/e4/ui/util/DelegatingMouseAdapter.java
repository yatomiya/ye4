/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.util.*;
import org.eclipse.swt.events.*;

public class DelegatingMouseAdapter implements MouseListener, MouseMoveListener {
    List<DelegatedMouseListener> listenerList;

    public DelegatingMouseAdapter() {
        listenerList = new ArrayList<DelegatedMouseListener>();
    }

    public List<DelegatedMouseListener> getListenerList() {
        return listenerList;
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        for (DelegatedMouseListener listener : listenerList) {
            listener.mouseDoubleClick(e);
            if (listener.isEventConsumed())
                break;
        }
    }

    @Override
    public void mouseDown(MouseEvent e) {
        for (DelegatedMouseListener listener : listenerList) {
            listener.mouseDown(e);
            if (listener.isEventConsumed())
                break;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        for (DelegatedMouseListener listener : listenerList) {
            listener.mouseUp(e);
            if (listener.isEventConsumed())
                break;
        }
    }

    @Override
    public void mouseMove(MouseEvent e) {
        for (DelegatedMouseListener listener : listenerList) {
            listener.mouseMove(e);
            if (listener.isEventConsumed())
                break;
        }
    }
}

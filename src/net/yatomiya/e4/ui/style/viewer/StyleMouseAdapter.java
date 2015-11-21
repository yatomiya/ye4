/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import org.eclipse.swt.events.*;
import net.yatomiya.e4.ui.style.*;

public abstract class  StyleMouseAdapter implements StyleMouseListener {
    @Override
    public void onEnter(StyleViewer viewer, StyleNode node, MouseEvent event) {
    }

    @Override
    public void onExit(StyleViewer viewer, StyleNode node) {
    }

    @Override
    public void onMove(StyleViewer viewer, StyleNode node, MouseEvent event) {
    }

    @Override
    public void onClick(StyleViewer viewer, StyleNode node, MouseEvent event) {
    }

    @Override
    public void onHover(StyleViewer viewer, StyleNode node, MouseEvent event) {
    }

    @Override
    public void onContextMenu(StyleViewer viewer, StyleNode node, MenuDetectEvent event) {
    }
}

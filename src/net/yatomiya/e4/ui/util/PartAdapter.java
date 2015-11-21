/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.workbench.modeling.*;

public abstract class PartAdapter implements IPartListener {

    @Override
    public void partActivated(MPart part) {
    }

    @Override
    public void partBroughtToTop(MPart part) {
    }

    @Override
    public void partDeactivated(MPart part) {
    }

    @Override
    public void partHidden(MPart part) {
    }

    @Override
    public void partVisible(MPart part) {
    }
}

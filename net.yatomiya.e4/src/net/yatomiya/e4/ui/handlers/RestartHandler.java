/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.e4.ui.workbench.*;


public class RestartHandler {
    @Execute
    public void execute(IWorkbench workbench) {
        workbench.restart();
    }
}

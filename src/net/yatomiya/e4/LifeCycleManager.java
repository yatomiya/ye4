/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4;

import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.ui.workbench.lifecycle.*;

public abstract class LifeCycleManager {
    IEclipseContext context;

    @PostContextCreate
    public void postContextCreate(IEclipseContext context) {
        Application app = createApplication();
        app.initialize(context);
    }

    @ProcessAdditions
    public void processAdditions(IEclipseContext context) {
    }

    @ProcessRemovals
    public void processRemovals(IEclipseContext context) {
    }

    @PreSave
    public void preSave(IEclipseContext context) {
        context.get(Application.class).shutdown();
    }

    protected abstract Application createApplication();
}


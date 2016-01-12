/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import javax.annotation.*;
import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class DummyHandler {
    @PostConstruct
    public void postConstruct(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
    }

    @Execute
    public void execute() {
    }
}

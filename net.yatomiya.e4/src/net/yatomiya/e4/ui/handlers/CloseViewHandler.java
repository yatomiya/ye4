/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import javax.inject.*;
import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.services.*;
import net.yatomiya.e4.services.part.*;

public class CloseViewHandler {
    @Execute
    public void execute(PartService partSrv,
                        @Named(IServiceConstants.ACTIVE_PART) @Optional MPart activePart) {
        partSrv.hidePart(activePart);
    }

    @CanExecute
    public boolean canExecute(PartService partSrv,
                              @Named(IServiceConstants.ACTIVE_PART) @Optional MPart activePart) {
        if (activePart != null
            && activePart.isCloseable()) {
            return true;
        }
        return false;
    }
}


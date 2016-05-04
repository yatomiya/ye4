/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.part;

import org.eclipse.e4.ui.workbench.modeling.*;

public enum PartState {
    CREATE(EPartService.PartState.CREATE),
    VISIBLE(EPartService.PartState.VISIBLE),
    ACTIVATE(EPartService.PartState.ACTIVATE);

    private EPartService.PartState eState;

    PartState(EPartService.PartState eState) {
        this.eState = eState;
    }

    EPartService.PartState getEPartServiceState() {
        return eState;
    }
}


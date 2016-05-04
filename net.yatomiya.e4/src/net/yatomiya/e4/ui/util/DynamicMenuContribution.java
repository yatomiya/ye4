/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.util.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import net.yatomiya.e4.util.*;

public class DynamicMenuContribution {
    public static MDirectMenuItem createMDirectMenuItem() {
        return EModelUtils.createElement(MDirectMenuItem.class);
    }

    public static void addDynamicMenu(
        List<MMenuElement> items,
        List<MMenuElement> addItems) {
        if (items.size() > 0) {
            items.add(EModelUtils.createElement(MMenuSeparator.class));
        }

        addItems = CUtils.map(addItems, e -> EModelUtils.cloneElement(e));

        items.addAll(addItems);
    }

    public static void addDynamicMenu(
        List<MMenuElement> items,
        MMenu menuHolder,
        String addMenuId) {
        MMenu addMenu = (MMenu)CUtils.findFirst(
            menuHolder.getChildren(),
            e -> ((Object)e instanceof MMenu) && e.getElementId().equals(addMenuId));
        if (addMenu != null) {
            addDynamicMenu(items, addMenu.getChildren());
        }
    }
}

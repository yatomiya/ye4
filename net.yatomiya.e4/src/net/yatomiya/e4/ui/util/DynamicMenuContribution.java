/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.util.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import net.yatomiya.e4.util.*;

public class DynamicMenuContribution {
    public static MDirectMenuItem createMDirectMenuItem() {
        return EModelUtils.createElement(MDirectMenuItem.class);
    }

    public static List<MMenuElement> createContextMenu(
        List<MMenu> menuList,
        String holderMenuId,
        String contextMenuId) {
        MMenu holderMenu = CUtils.findFirst(menuList, e -> e.getElementId().equals(holderMenuId));
        if (holderMenu == null)
            return Collections.EMPTY_LIST;
        MMenu contextMenu = (MMenu)CUtils.findFirst(holderMenu.getChildren(), e -> e.getElementId().equals(contextMenuId));
        if (contextMenu == null)
            return Collections.EMPTY_LIST;

        return CUtils.map(contextMenu.getChildren(), e -> EModelUtils.cloneElement(e));
    }

    public static List<MMenuElement> addContextMenuBlock(
        List<MMenuElement> list,
        List<MMenu> menuList,
        String holderMenuId,
        String contextMenuId) {
        if (list.size() > 0) {
            list.add(EModelUtils.createElement(MMenuSeparator.class));
        }

        List<MMenuElement> elements = createContextMenu(menuList, holderMenuId, contextMenuId);
        list.addAll(elements);

        return elements;
    }

    public static List<MMenuElement> addContextMenuBlock(
        List<MMenuElement> list,
        MPart part,
        String holderMenuId,
        String contextMenuId) {
        return addContextMenuBlock(list, part.getMenus(), holderMenuId, contextMenuId);
    }
}


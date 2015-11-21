/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.util.*;
import org.eclipse.e4.ui.di.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import net.yatomiya.e4.util.*;

/**
ポップアップメニューを選択しても、SWTセレクションイベントが発生しないバグがある。対処法は以下。

in org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer
scheduleManagerUpdate()
		// Bug 467000: Avoid repeatedly updating menu managers
		// This workaround is opt-in for 4.5
		boolean workaroundEnabled = Boolean.getBoolean("eclipse.workaround.bug467000"); //$NON-NLS-1$
		if (!workaroundEnabled) {
			mgr.update(false);
			return;
		}
上記の mgr.update() がメニューの Selection SWTイベントより先に呼ばれてしまい、その中で dispose されてしまうため
Selection イベントが呼ばれなくなっている。システムプロパティで eclipse.workaround.bug467000=true にすると、上手くいく。

 */

public class DynamicMenuContribution {
    @AboutToShow
    public void aboutToShow(List<MMenuElement> items) {
    }

    @AboutToHide
    public void aboutToHide(List<MMenuElement> items) {
        if (items != null) {
            for (MMenuElement item : items) {
                MMenu parent = (MMenu)(Object)item.getParent();
                parent.getChildren().remove(item);
            }
        }
    }

    public static MDirectMenuItem createMDirectMenuItem() {
        return ModelUtils.createElement(MDirectMenuItem.class);
    }

    public static List<MMenuElement> createPartContextMenu(
        MPart part,
        String holderMenuId,
        String contextMenuId) {
        MMenu holderMenu = CUtils.findFirst(part.getMenus(), e -> e.getElementId().equals(holderMenuId));
        if (holderMenu == null)
            return Collections.EMPTY_LIST;
        MMenu contextMenu = (MMenu)CUtils.findFirst(holderMenu.getChildren(), e -> e.getElementId().equals(contextMenuId));
        if (contextMenu == null)
            return Collections.EMPTY_LIST;

        return CUtils.map(contextMenu.getChildren(), e -> ModelUtils.cloneElement(e));
    }

    public static void addContextMenuBlock(
        List<MMenuElement> list,
        MPart part,
        String holderMenuId,
        String contextMenuId) {
        if (list.size() > 0) {
            list.add(ModelUtils.createElement(MMenuSeparator.class));
        }

        list.addAll(createPartContextMenu(part, holderMenuId, contextMenuId));
    }
}


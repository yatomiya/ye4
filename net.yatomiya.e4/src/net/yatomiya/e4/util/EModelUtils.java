/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;
import java.util.List;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.application.descriptor.basic.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.advanced.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import org.eclipse.e4.ui.services.*;
import org.eclipse.e4.ui.workbench.*;
import org.eclipse.e4.ui.workbench.modeling.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.*;
import org.eclipse.swt.widgets.*;

public class EModelUtils {
    public static EModelService getService() {
        return EUtils.get(EModelService.class);
    }

    public static <T extends MApplicationElement> T createElement(Class<T> clazz) {
        return getService().createModelElement(clazz);
    }

    public static MApplication getApplication() {
        return EUtils.get(MApplication.class);
    }

    public static MUIElement findSnippet(String id) {
        return getService().findSnippet(getApplication(), id);
    }

    public static MPartDescriptor getPartDescriptor(String id) {
        return getService().getPartDescriptor(id);
    }

    public static <T extends MMenuElement> List<T> findMenus(MPart part, Class<T> clazz, Selector selector) {
        List<T> list = new ArrayList<>();
        for (MMenu m : part.getMenus()) {
            list.addAll(findElements(m, clazz, selector));
        }
        return list;
    }

    public static <T> List<T> findElements(MUIElement searchRoot, Class<T> clazz, int searchFlags, Selector selector) {
        if (selector == null)
            selector = (e) -> { return true; };

        return getService().findElements(searchRoot, clazz, searchFlags, selector);
    }

    public static <T> List<T> findElements(MUIElement searchRoot, Class<T> clazz, Selector selector) {
        return findElements(searchRoot, clazz, EModelService.ANYWHERE, selector);
    }

    public static <T> List<T> findElements(Class<T> clazz, Selector selector) {
        MApplication app = getApplication();
        return findElements(app, clazz, EModelService.ANYWHERE, selector);
    }

    public static <T> List<T> findElements(MUIElement searchRoot, Class<T> clazz) {
        return findElements(searchRoot, clazz, EModelService.ANYWHERE, null);
    }

    public static <T> List<T> findElements(Class<T> clazz) {
        return findElements(clazz, null);
    }

    public static MUIElement find(String id, MUIElement searchRoot) {
        return getService().find(id, searchRoot);
    }

    public static <T extends MUIElement> T cloneElement(T src) {
        if (!(src instanceof EObject)) {
            throw new IllegalArgumentException("");
        }

        return (T)EcoreUtil.copy((EObject)src);
    }

    public static void copyMMenu(List<MMenuElement> dst, List<MMenu> srcMenuList, String contextMenuId) {
        MMenu srcMenu = null;
        for (MMenu menu : srcMenuList) {
            if (menu.getElementId().equals(contextMenuId)) {
                srcMenu = menu;
                break;
            }
        }
        if (srcMenu == null)
            return;

        for (MMenuElement m : srcMenu.getChildren()) {
            dst.add(cloneElement(m));
        }
    }

    public static MUIElement cloneSnippet(String id) {
        return getService().cloneSnippet(getApplication(), id, null);
    }

    public static MPart getActivePart() {
        return (MPart)EUtils.get(IServiceConstants.ACTIVE_PART);
    }

    public static MPerspective getActivePerspective() {
        List<MPerspectiveStack> list = findElements(
            getApplication(), MPerspectiveStack.class, EModelService.PRESENTATION, null);
        if (list.size() > 0)
            return list.get(0).getSelectedElement();
        return null;
    }

    public static MWindow getActiveWindow() {
        Shell activeShell = (Shell)EUtils.get(IServiceConstants.ACTIVE_SHELL);
        for (MWindow win : findElements(MWindow.class)) {
            if (win.getWidget() == activeShell)
                return win;
        }
        return null;
    }

    public static MPart getPartFor(MUIElement element) {
        MUIElement e = element;
        EModelService srv = getService();

        while (e != null) {
            if (e instanceof MPart) {
                return (MPart)e;
            }
            e = srv.getContainer(e);
        }

        return null;
    }

    public static MPerspective getPerspectiveFor(MUIElement element) {
        return getService().getPerspectiveFor(element);
    }

    public static MWindow getWindowFor(MUIElement element) {
        return getService().getTopLevelWindowFor(element);
    }
}


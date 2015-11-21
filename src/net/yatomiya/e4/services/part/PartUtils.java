/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.part;

import java.util.*;
import java.util.function.*;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.workbench.*;
import org.eclipse.e4.ui.workbench.modeling.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;

public class PartUtils {
    private static PartService partService;

    private PartUtils() {
    }

    public static String getUUID(MPart part) {
        return part.getPersistedState().get(PartService.PART_UUID_KEY);
    }

    public static final Selector ALL_SELECTOR = new Selector() {
        @Override
        public boolean select(MApplicationElement element) {
            return true;
        }
    };

    private static PartService getService() {
        if (partService == null) {
            partService = AppUtils.get(PartService.class);
        }
        return partService;
    }

    public static MPart findPart(Function<MPart, Boolean> selector) {
        List<MPart> list = findParts(selector);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static List<MPart> listParts() {
        return findParts((part) -> true);
    }

    public static List<MPart> findParts(Function<MPart, Boolean> selector) {
        return ModelUtils.findElements(
            ModelUtils.getActivePerspective(),
            MPart.class,
            EModelService.IN_ACTIVE_PERSPECTIVE,
            element -> selector.apply(((MPart)element)));
    }

    public static MPart findPart(String id) {
        List<MPart> list = findParts(id);
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    public static List<MPart> findParts(String id) {
        return findParts(part -> part.getElementId().equals(id));
    }

    public static List<MPart> findVisibleParts() {
        return findParts(part -> part.isVisible() && part.isToBeRendered());
    }

    public static void openPartsWithFirstVisible(List<MPart> partList) {
        for (int i = 0; i < partList.size(); i++) {
            PartService.PartState state = i == 0 ? PartService.PartState.ACTIVATE : PartService.PartState.CREATE;
            getService().openPart(partList.get(i), state);
        }
    }

    public static void disposeElement(MUIElement element) {
        setToBeRenderedAll(element, false);

        MElementContainer container = element.getParent();
        if (container != null)
            container.getChildren().remove(element);
    }

    public static <T extends MUIElement> void disposeElements(List<T> elements) {
        for (MUIElement e : new ArrayList<>(elements)) {
            PartUtils.disposeElement(e);
        }
    }

    public static void setToBeRenderedAll(MUIElement element, boolean value) {
        element.setToBeRendered(value);
        if (element instanceof MElementContainer) {
            MElementContainer<MUIElement> container = (MElementContainer<MUIElement>)element;
            for (MUIElement c : container.getChildren()) {
                setToBeRenderedAll(c, value);
            }
        }
    }


}


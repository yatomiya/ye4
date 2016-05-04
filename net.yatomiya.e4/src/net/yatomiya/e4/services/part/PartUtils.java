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
import java.util.stream.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.workbench.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public class PartUtils {
    private static PartService partService;

    public static String getUUID(MPart part) {
        return part.getPersistedState().get(PartService.PART_UUID_KEY);
    }

    private static PartService getService() {
        if (partService == null) {
            partService = EUtils.get(PartService.class);
        }
        return partService;
    }

    public static MPart openPart(String descId) {
        return openPart(descId, true);
    }

    public static MPart openPart(String descId, boolean showIfExists) {
        return openPart(descId, showIfExists, PartState.ACTIVATE);
    }

    public static MPart openPart(String descId, boolean showIfExists, PartState state) {
        return openPart(descId, showIfExists, state, PartService.PART_CONTAINER_TYPE_DEFAULT);
    }

    public static MPart openPart(String descId, boolean showIfExists, PartState state, String containerType) {
        return openPart(descId, showIfExists, state, PartService.PART_CONTAINER_TYPE_DEFAULT, null);
    }

    public static MPart openPart(String descId, boolean showIfExists, PartState state, String containerType, Consumer<MPart> initializer) {
        PartService service = getService();
        MPart part = null;
        if (showIfExists) {
            part = service.findPart(descId);
        }
        if (part == null) {
            part = service.createPart(descId);
        }

        if (part != null) {
            if (initializer != null) {
                initializer.accept(part);
            }

            service.openPart(part, state, containerType);
        }
        return part;
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

    public static void subscribePartActivate(Object tag, Consumer<MPart> func) {
        EUtils.get(EventService.class).subscribe(
            tag,
            UIEvents.UILifeCycle.ACTIVATE,
            event -> {
                EventService.UIEventData e = event.getUIEventData();
                if (e != null
                    && e.element instanceof MPart) {
                    func.accept((MPart)e.element);
                }
            });
    }

    public static void subscribePartClosed(Object tag, Consumer<MPart> func) {
        EUtils.get(EventService.class).subscribe(
            tag,
            UIEvents.UIElement.TOPIC_TOBERENDERED,
            event -> {
                EventService.UIEventData e = event.getUIEventData();
                if (e != null
                    && e.element instanceof MPart
                    && e.isSET()
                    && (Boolean)e.newValue == Boolean.FALSE) {
                    func.accept((MPart)e.element);
                }
            });
    }

    public static List<MPart> getCloseableParts(MPartStack stack) {
        return stack.getChildren().stream()
            .filter(e -> (e instanceof MPart) && (((MPart)e).isCloseable()))
            .map(e -> (MPart)e)
            .collect(Collectors.toList());
    }

    public static void closeTabAll(MPartStack stack) {
        for (MPart part : getCloseableParts(stack)) {
            getService().hidePart(part);
        }
    }

    public static void closeOtherTabAll(MPart part) {
        if ((Object)part.getParent() instanceof MPartStack) {
            MPartStack parent = (MPartStack)(Object)part.getParent();
            for (MPart e : getCloseableParts(parent)) {
                if (e != part) {
                    getService().hidePart(e);
                }
            }
        }
    }
}


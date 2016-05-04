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
import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.advanced.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.workbench.*;
import org.eclipse.e4.ui.workbench.modeling.*;
import org.eclipse.swt.custom.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public class PartService {
    public static final String WINDOW_ID_MAIN = Application.PLUGIN_ID + ".window.main";
    public static final String PERSPECTIVE_STACK_ID = Application.PLUGIN_ID + ".perspectivestack";

    public static final String PART_UUID_KEY = Application.PLUGIN_ID + ".uuid";
    public static final String PART_CONTAINER_TYPE = Application.PLUGIN_ID + ".part.container.type";
    public static final String PART_CONTAINER_TYPE_DEFAULT = "default";

    public static interface Event {
        public static final String BASE = "net/yatomiya/e4/services/part/";
        public static final String ALL = BASE + "*";

        public static interface Part {
            public static final String BASE = Event.BASE + "part/";
            public static final String ALL = BASE + "*";
            public static final String CREATE = BASE + "create";
            public static final String OPEN = BASE + "open";
            public static final String HIDE = BASE + "hide";
        }
    }

    private IEclipseContext context;
    private Application application;
    private MApplication mapp;
    private PerspectiveManager persMgr;

    public void initialize(IEclipseContext context) {
        this.context = context;
        this.application = context.get(Application.class);

        mapp = context.get(MApplication.class);

        addEventHandlers();
    }

    void addEventHandlers() {
        /**
         * Customize CTabFolder used with PartStack.
         */
        context.get(EventService.class).subscribe(
            this, UIEvents.UIElement.TOPIC_WIDGET, EventService.UIEventData.class,
            data -> {
                if (data.element instanceof MPartStack) {
                    MPartStack stack = (MPartStack)data.element;
                    if (stack.getWidget() instanceof CTabFolder) {
                        CTabFolder f = (CTabFolder)stack.getWidget();
                        f.setMinimumCharacters(10);
                        f.setUnselectedCloseVisible(true);
                    }
                }
            });
    }


    public void initializeUI() {
        persMgr = new PerspectiveManager(this);
    }

    public void shutdown() {
        context.get(EventService.class).unsubscribe(this);
    }

    public IEclipseContext getContext() {
        return context;
    }

    public EPartService getE4Service() {
        return context.get(EPartService.class);
    }

    public PerspectiveManager getPerspectiveManager() {
        return persMgr;
    }

    public MPart getActivePart() {
        return EModelUtils.getActivePart();
    }

    public MPerspective getActivePerspective() {
        return EModelUtils.getActivePerspective();
    }

    public void activate(MPart part) {
        getE4Service().activate(part);
    }

    public void hidePart(MPart part) {
        getE4Service().hidePart(part, true);

        context.get(EventService.class).post(Event.Part.HIDE, part);
    }

    public void bringToTop(MPart part) {
        getE4Service().bringToTop(part);
    }

    public boolean isPartVisible(MPart part) {
        return getE4Service().isPartVisible(part);
    }

    public MPart createPart(String descId) {
        MPart part = getE4Service().createPart(descId);
        if (part == null) {
            throw new IllegalStateException("Failed to create part with descriptor " + descId);
        }

        /** By default, closing part by pressing close button does not remove MPart, just set toBeRendered false.
            To remove part from model, this tag is needed to add. **/
        part.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);

        String sid = UUID.randomUUID().toString().replace("-", "");
        part.getPersistedState().put(PART_UUID_KEY, sid);

        context.get(EventService.class).post(Event.Part.CREATE, part);

        return part;
    }

    public MPart openPart(MPart part) {
        return openPart(part, PartState.ACTIVATE);
    }

    public MPart openPart(MPart part, PartState state) {
        return openPart(part, state, PART_CONTAINER_TYPE_DEFAULT);
    }

    public MPart openPart(MPart part, PartState state, String containerType) {
        if (state == null)
            state = PartState.ACTIVATE;

        if (JUtils.isEmpty(containerType))
            containerType = PART_CONTAINER_TYPE_DEFAULT;
        part.getPersistedState().put(PartService.PART_CONTAINER_TYPE, containerType);

        if (part.getParent() == null) {
            MPerspective pers = getPerspectiveManager().getActivePerspective();
            MElementContainer container = PerspectiveManager.findPreferredPartContainer(pers, part);

            if (container == null) {
                return getE4Service().showPart(part, state.getEPartServiceState());
            }

            container.getChildren().add(part);
        }

        switch (state) {
        case CREATE:
            break;
        case ACTIVATE:
            activate(part);
            break;
        case VISIBLE:
            bringToTop(part);
            break;
        }

        context.get(EventService.class).post(Event.Part.OPEN, part);

        return part;
    }

    public MPart findPart(Function<MPart, Boolean> selector) {
        List<MPart> list = findParts(selector);
        return list.size() > 0 ? list.get(0) : null;
    }

    public List<MPart> listParts() {
        return findParts((part) -> true);
    }

    public List<MPart> findParts(Function<MPart, Boolean> selector) {
        return EModelUtils.findElements(
            EModelUtils.getActivePerspective(),
            MPart.class,
            EModelService.IN_ACTIVE_PERSPECTIVE,
            element -> selector.apply(((MPart)element)));
    }

    public MPart findPart(String descId) {
        List<MPart> list = findParts(descId);
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    public List<MPart> findParts(String descId) {
        return findParts(part -> part.getElementId().equals(descId));
    }

    public List<MPart> findVisibleParts() {
        return findParts(part -> part.isVisible() && part.isToBeRendered());
    }

    void switchPerspective(MPerspective perspective) {
        getE4Service().switchPerspective(perspective);
    }
}


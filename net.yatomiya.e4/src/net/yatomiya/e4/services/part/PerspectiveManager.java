/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.part;

import java.util.*;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.advanced.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.workbench.modeling.*;
import net.yatomiya.e4.util.*;

public class PerspectiveManager {
    private PartService service;
    private MPerspectiveStack persStack;
    private String currentPerspectiveId;

    PerspectiveManager(PartService service) {
        this.service = service;
        currentPerspectiveId = null;

        MApplication mapp = EModelUtils.getApplication();
        MTrimmedWindow mwin = (MTrimmedWindow)EModelUtils.find(PartService.WINDOW_ID_MAIN, mapp);
        persStack = (MPerspectiveStack)EModelUtils.find(PartService.PERSPECTIVE_STACK_ID, mwin);
    }

    public PartService getService() {
        return service;
    }

    public MPerspective getActivePerspective() {
        return EModelUtils.getActivePerspective();
    }

    public boolean isActivePerspective(String id) {
        MPerspective pers = getActivePerspective();
        return pers != null && pers.getElementId().equals(id);
    }

    public List<MPerspective> getPerspectives() {
        return Collections.unmodifiableList(persStack.getChildren());
    }

    public MPerspective getPerspective(String id) {
        return (MPerspective)EModelUtils.find(id, persStack);
    }

    public MPerspective createPerspective(String id) {
        MPerspective pers = getPerspective(id);
        if (pers != null)
            return pers;

        pers = (MPerspective)EModelUtils.cloneSnippet(id);
        if (pers == null)
            return null;
        persStack.getChildren().add(pers);
        return pers;
    }

    public void removePerspective(String id) {
        MPerspective pers = getPerspective(id);
        if (pers == null)
            return;

        persStack.getChildren().remove(pers);
    }

    public void switchPerspective(String id, boolean createIfNotExists) {
        MPerspective pers = getPerspective(id);
        if (pers == null) {
            pers = createPerspective(id);
            if (id == null)
                return;
        }

        switchPerspective(pers);
    }

    public void switchPerspective(MPerspective pers) {
        currentPerspectiveId = pers.getElementId();

        service.switchPerspective(pers);
    }

    public void resetPerspective() {
        resetPerspective(getActivePerspective(), currentPerspectiveId);
    }

    // Creats new perspective and move all parts to new one. Current pers is removed.
    MPerspective resetPerspective(MPerspective pers, String snippetPerspectiveId) {
        List<MPart> partList = EModelUtils.findElements(pers, MPart.class);

        List<MWindow> oldWindows = new ArrayList<>(pers.getWindows());

        MPerspective newPers = (MPerspective)EModelUtils.cloneSnippet(snippetPerspectiveId);
        persStack.getChildren().add(newPers);

        EModelService modelService = EModelUtils.getService();
        for (MPart part : partList) {
            MElementContainer container = findPreferredPartContainer(newPers, part);
            modelService.move(part, container);
        }

        switchPerspective(newPers);

        persStack.getChildren().remove(pers);

        return newPers;
    }

    public static MElementContainer findPreferredPartContainer(MPerspective pers, MPart part) {
        String type = part.getPersistedState().get(PartService.PART_CONTAINER_TYPE);
        if (JUtils.isEmpty(type))
            return null;

        MElementContainer container = findPartContainer(pers, type);
        if (container == null) {
            container = findPartContainer(pers, PartService.PART_CONTAINER_TYPE_DEFAULT);
        }
        return container;
    }

    public static MElementContainer findPartContainer(MPerspective pers, String type) {
        List<MElementContainer> containerList = EModelUtils.findElements(pers, MElementContainer.class);
        for (MElementContainer c : containerList) {
            String containerType = c.getPersistedState().get(PartService.PART_CONTAINER_TYPE);
            if (JUtils.isNotEmpty(containerType)
                && type.equals(containerType))
                return c;
        }
        return null;
    }

    public static ListBundle<String, MPart> listPartsByContainerType(MPerspective pers) {
        return listPartsByContainerType(EModelUtils.findElements(pers, MPart.class));
    }

    public static ListBundle<String, MPart> listPartsByContainerType(List<MPart> parts) {
        ListBundle<String, MPart> partBundle = new ListBundle<>();

        for (MPart part : parts) {
            String type = part.getPersistedState().get(PartService.PART_CONTAINER_TYPE);
            if (JUtils.isEmpty(type)) {
                partBundle.add(PartService.PART_CONTAINER_TYPE_DEFAULT, part);
            } else {
                partBundle.add(type, part);
            }
        }
        return partBundle;
    }

}

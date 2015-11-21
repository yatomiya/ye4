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
import net.yatomiya.e4.util.*;

public class PerspectiveManager {
    private PartService service;
    private MPerspectiveStack persStack;

    PerspectiveManager(PartService service) {
        this.service = service;

        MApplication mapp = ModelUtils.getApplication();
        MTrimmedWindow mwin = (MTrimmedWindow)ModelUtils.find(PartService.WINDOW_ID_MAIN, mapp);
        persStack = (MPerspectiveStack)ModelUtils.find(PartService.PERSPECTIVE_STACK_ID, mwin);
    }

    public PartService getService() {
        return service;
    }

    public MPerspective getActivePerspective() {
        return ModelUtils.getActivePerspective();
    }

    public boolean isActivePerspective(String id) {
        MPerspective pers = getActivePerspective();
        return pers != null && pers.getElementId().equals(id);
    }

    public List<MPerspective> getPerspectives() {
        return Collections.unmodifiableList(persStack.getChildren());
    }

    public MPerspective getPerspective(String id) {
        return (MPerspective)ModelUtils.find(id, persStack);
    }

    public MPerspective createPerspective(String id) {
        MPerspective pers = getPerspective(id);
        if (pers != null)
            return pers;

        pers = (MPerspective)ModelUtils.cloneSnippet(id);
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
        service.switchPerspective(pers);
    }

    public void resetPerspective(MPerspective pers, String snippetPerspectiveId) {
        ListBundle<String, MPart> partBundle = listPartsByContainerType(pers);
        List<MPart> partList = partBundle.getElementsAll();
        for (MPart part : partList) {
            part.getParent().getChildren().remove(part);
        }

        for (MWindow w : new ArrayList<>(pers.getWindows())) {
            PartUtils.disposeElement(w);
        }
        for (MUIElement c : new ArrayList<>(pers.getChildren())) {
            PartUtils.disposeElement(c);
        }

        MPerspective snippetPers = (MPerspective)ModelUtils.cloneSnippet(snippetPerspectiveId);
        pers.getWindows().addAll(snippetPers.getWindows());
        pers.getChildren().addAll(snippetPers.getChildren());

        for (MPart part : partList) {
            getService().openPart(part);
        }
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
        List<MElementContainer> containerList = ModelUtils.findElements(pers, MElementContainer.class);
        try {
            return containerList.stream().
                filter(e -> {
                        String containerType = e.getPersistedState().get(PartService.PART_CONTAINER_TYPE);
                        return JUtils.isNotEmpty(containerType) && type.equals(containerType);
                    }).
                findFirst().
                get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static ListBundle<String, MPart> listPartsByContainerType(MPerspective pers) {
        ListBundle<String, MPart> partBundle = new ListBundle<>();

        for (MPart part : ModelUtils.findElements(pers, MPart.class)) {
            String type = part.getPersistedState().get(PartService.PART_CONTAINER_TYPE_DEFAULT);
            if (JUtils.isEmpty(type)) {
                partBundle.add(PartService.PART_CONTAINER_TYPE_DEFAULT, part);
            } else {
                partBundle.add(type, part);
            }
        }
        return partBundle;
    }

}

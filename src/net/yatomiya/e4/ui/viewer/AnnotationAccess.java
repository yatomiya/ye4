/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import org.eclipse.jface.text.source.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class AnnotationAccess implements IAnnotationAccess, IAnnotationAccessExtension {
    @Override
    public String getTypeLabel(Annotation annotation) {
        return annotation.getText();
    }

    @Override
    public int getLayer(Annotation annotation) {
        if (annotation instanceof IAnnotationPresentation) {
            return ((IAnnotationPresentation)annotation).getLayer();
        }

        return IAnnotationAccessExtension.DEFAULT_LAYER;
    }

    @Override
    public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {
        if (annotation instanceof IAnnotationPresentation) {
            ((IAnnotationPresentation)annotation).paint(gc, canvas, bounds);
            return;
        }
    }

    @Override
    public boolean isPaintable(Annotation annotation) {
        if (annotation instanceof IAnnotationPresentation) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isSubtype(Object annotationType, Object potentialSupertype) {
        return annotationType.equals(potentialSupertype);
    }

    @Override
    public Object[] getSupertypes(Object annotationType) {
        return null;
    }

    @Override
    public Object getType(Annotation annotation) {
        return null;
    }

    @Override
    public boolean isMultiLine(Annotation annotation) {
        return true;
    }

    @Override
    public boolean isTemporary(Annotation annotation) {
        return false;
    }
}

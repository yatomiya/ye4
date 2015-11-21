/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.util.function.*;
import org.eclipse.jface.viewers.*;


public class ViewerUtils {
    public static boolean visit(ITreeContentProvider contentProvider, Object rootElement, Predicate<Object> func) {
        boolean result;
        result = func.test(rootElement);
        if (result == false)
            return result;

        Object[] children = contentProvider.getChildren(rootElement);
        if (children != null) {
            for (Object c : children) {
                result = visit(contentProvider, c, func);
            }
        }
        return result;
    }


}

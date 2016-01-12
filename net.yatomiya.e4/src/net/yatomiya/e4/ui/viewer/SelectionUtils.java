/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.util.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.viewers.*;
import net.yatomiya.e4.util.*;

public class SelectionUtils {
    private SelectionUtils() {
    }

    public static int size(ISelection selection) {
        if (selection != null && selection instanceof IStructuredSelection)
            return ((IStructuredSelection)selection).size();
        return 0;
    }

    public static boolean isEmpty(ISelection selection) {
        return size(selection) == 0;
    }

    public static boolean isNotEmpty(ISelection selection) {
        return !isEmpty(selection);
    }

    public static List<Object> toList(ISelection selection) {
        if (selection != null && selection instanceof IStructuredSelection) {
            return ((IStructuredSelection)selection).toList();
        }
        return Collections.EMPTY_LIST;
    }

    public static <T> List<T> toList(ISelection selection, Class<T> clazz) {
        return CUtils.convertList(toList(selection), clazz);
    }

    public static Object getFirstElement(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            return ((IStructuredSelection)selection).getFirstElement();
        }
        return null;
    }

    public static <T> List<T> extract(ISelection selection, Class<T> clazz) {
        return CUtils.extract(toList(selection), clazz);
    }

    public static <T> boolean hasAny(ISelection selection, Class<T> clazz) {
        return CUtils.anyMatch(toList(selection), e -> clazz.isInstance(e));
    }

    public static String getStringFrom(TextSelection selection) {
        String s = JUtils.nonNull(selection.getText());
        s = StringUtils.removeObjectReplacementCharacter(s);
        return s;
    }

    public static boolean isEmpty(TextSelection selection) {
        if (selection != null) {
            String s = getStringFrom(selection);
            return JUtils.isEmpty(s);
        }
        return true;
    }

    public static boolean isNotEmpty(TextSelection selection) {
        return !isEmpty(selection);
    }
}


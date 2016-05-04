/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.ui.workbench.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.*;

public class EUtils {
    private EUtils() {
    }

    public static Application getApplication() {
        return Application.getInstance();
    }

    public static IEclipseContext getContext() {
        return getApplication().getContext();
    }

    public static <T> T get(Class<T> clazz) {
        return getApplication().get(clazz);
    }

    public static Object get(String key) {
        return getApplication().get(key);
    }

    public static Map<String,Object> getEclipseContextValues(IEclipseContext context) {
        Map<String,Object> map = new HashMap<>();
        IEclipseContext current = context;

        Class<?> clz = current.getClass();
        current = current.getActiveLeaf();
        do {
            try {
                Map<String,Object> localMap = null;
                Method method = clz.getMethod("localData");
                localMap = (Map<String,Object>)method.invoke(current);
                for (String key : localMap.keySet()) {
                    if (!map.containsKey(key))
                        map.put(key, localMap.get(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while ((current = current.getParent()) != null);

        return map;
    }

    public static void printContextValues(IEclipseContext context) {
        Map<String, Object> map = getEclipseContextValues(context);
        for (String key : map.keySet()) {
            JUtils.println(String.format("%s : [%s]", key, map.get(key)));
        }
    }

    public static void exit() {
        get(IWorkbench.class).close();
    }

    public static void restart() {
        get(IWorkbench.class).restart();
    }

    public static Display getDisplay() {
        return Display.getDefault();
    }

    public static Thread getUIThread() {
        return getDisplay().getThread();
    }

    public static boolean isUIThread() {
        return getUIThread() == Thread.currentThread();
    }

    public static void syncExec(Runnable runner) {
        getDisplay().syncExec(runner);
    }

    public static void asyncExec(Runnable runner) {
        getDisplay().asyncExec(runner);
    }

    public static void timerExec(long startms, Runnable runner) {
        getDisplay().timerExec((int)startms, runner);
    }

    public static void saveApplicationModel() throws IOException {
        get(IModelResourceHandler.class).save();
    }
}

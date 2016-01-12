/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services;

import java.io.*;
import java.util.*;
import java.util.function.*;
import org.eclipse.e4.core.contexts.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;

public class PersistenceService {
    private IEclipseContext context;
    private Application application;
    private Map<String, Object> dataMap;
    private RepeatableRunner backgroundSaver;

    public void initialize(IEclipseContext context) {
        this.context = context;
        application = context.get(Application.class);
        dataMap = new HashMap<>();

        load();

        backgroundSaver = new RepeatableRunner((int)(Math.random() * (1000*60*5)), 1000*60*5, () -> save());
    }

    public void shutdown() {
        backgroundSaver.cancel();

        save();
    }

    public void put(String key, Object value) {
        if (value == null)
            throw new IllegalArgumentException("value is null.");

        dataMap.put(key, value);
    }

    public boolean remove(String key) {
        return dataMap.remove(key) != null;
    }

    public Object get(String key) {
        return dataMap.get(key);
    }

    public <T> T get(String key, Supplier<T> func) {
        T obj = (T)get(key);
        if (obj == null) {
            obj = func.get();
            put(key, obj);
        }
        return obj;
    }

    public <T> T get(String key, T initialValue) {
        return get(key, () -> initialValue);
    }

    public File getStorageFile() {
        return new File(application.getDataPath(), "pobj.ser");
    }

    public void save() {
        Map<String, String> map = new HashMap<>();
        for (String key : dataMap.keySet()) {
            Object o = dataMap.get(key);
            String str = IOUtils.serializeObject(o);
            map.put(key, str);
        }

        try {
            IOUtils.writeObject(getStorageFile(), map, true);
        } catch (IOException e) {
            LogService.warn(LogService.Category.IO, e, "PersistenceService のセーブに失敗しました。");
        }
    }

    public void load() {
        File file = getStorageFile();
        Map<String, String> map = null;
        if (IOUtils.isFileExists(file)) {
            try {
                map = (Map<String, String>)IOUtils.readObject(file, true);
            } catch (IOException e) {
                LogService.warn(LogService.Category.IO, e, "PersistenceService のロードに失敗しました。");
                return;
            }
        }

        if (map != null) {
            dataMap = new HashMap<>();

            for (String key : map.keySet()) {
                String str = map.get(key);

                try {
                    Object o = IOUtils.deserializeObject(str);
                    dataMap.put(key, o);
                } catch (IOException e) {
                    // just skip
                }
            }
        }
    }

}



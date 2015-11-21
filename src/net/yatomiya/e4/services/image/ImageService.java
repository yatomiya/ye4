/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.image;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.eclipse.e4.core.contexts.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;

public class ImageService {
    public static final String ROOT_PATH = "image";

    private IEclipseContext context;
    private File rootDirectory;

    private StorageManager storageManager;

    private Map<HttpUrl, CacheEntry> entryMap;

    private QueueThreadExecutor storageUpdateExecutor;

    private PriorityBlockingQueue<Runnable> priorityQueue;
    private ThreadPoolExecutor httpThreadExecutor;
    private StandardHttpClient httpClient;

    public void initialize(IEclipseContext context) {
        this.context = context;

        entryMap = new HashMap<>();

        Application app = context.get(Application.class);

        rootDirectory = new File(app.getDataPath(), ROOT_PATH);
        IOUtils.checkAndCreateDirectory(rootDirectory);

        storageManager = new StorageManager(this);
        storageManager.load();

        storageUpdateExecutor = new QueueThreadExecutor(16);

        {
            priorityQueue = new PriorityBlockingQueue<Runnable>(
                16, new StandardHttpClient.CallComparator<UpdateHandler>((o1,o2) -> o1.compareTo(o2)));
            httpThreadExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, priorityQueue);
            httpClient = new StandardHttpClient();
            httpClient.setDispatcher(new Dispatcher(httpThreadExecutor));
        }
    }

    public void shutdown() {
        httpClient.cancelAll();

        for (CacheEntry entry : entryMap.values()) {
            entry.disposeImagesAll();
        }
        entryMap.clear();

        storageManager.save();
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    StorageManager getStorageManager() {
        return storageManager;
    }

    QueueThreadExecutor getStorageUpdateExecutor() {
        return storageUpdateExecutor;
    }

    StandardHttpClient getHttpClient() {
        return httpClient;
    }

    public CacheEntry subscribe(String url, ImageType type, CacheListener listener) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null)
            throw new IllegalArgumentException("");
        return subscribe(httpUrl, type, listener);
    }

    public CacheEntry subscribe(HttpUrl url, ImageType type, CacheListener listener) {
        CacheEntry entry = entryMap.get(url);
        if (entry == null) {
            entry = new CacheEntry(this, url);
            entryMap.put(url, entry);
        }

        entry.subscribe(type, listener);

        if (!entry.hasImages(type)
            && !entry.isUpdating())
            entry.update();

        return entry;
    }


    public CacheEntry subscribe(HttpUrl url, CacheListener listener) {
        return subscribe(url, ImageType.ORIGINAL, listener);
    }

    public CacheEntry subscribeThumbnail(HttpUrl url, CacheListener listener) {
        return subscribe(url, ImageType.THUMBNAIL, listener);
    }

    public void unsubscribe(CacheEntry entry, ImageType type, CacheListener listener) {
        if (entryMap.get(entry.getUrl()) == null)
            throw new IllegalArgumentException("entry is not subscribed.");

        entry.unsubscribe(type, listener);

        if (!entry.hasCacheListener()) {
            entryMap.remove(entry.getUrl());
        }
    }

    public Map<HttpUrl, CacheEntry> getCacheEntryMap() {
        return Collections.unmodifiableMap(entryMap);
    }

}


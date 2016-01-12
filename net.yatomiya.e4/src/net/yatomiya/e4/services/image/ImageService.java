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
import org.eclipse.swt.graphics.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;
import net.yatomiya.e4.ui.image.*;
import net.yatomiya.e4.ui.util.*;

public class ImageService {
    public static final String ROOT_PATH = "image";

    private IEclipseContext context;
    private File rootDirectory;

    private StorageManager storageManager;

    private WeakValueHashMap<HttpUrl, ImageEntry> entryMap;

    private QueueThreadExecutor storageUpdateExecutor;

    private PriorityBlockingQueue<Runnable> priorityQueue;
    private ThreadPoolExecutor httpThreadExecutor;
    private StandardHttpClient httpClient;

    private UndisposedImagesCleaner undisposedImagesCleaner;
    private RepeatableRunner backgroundSaver;

    private Point thumbnailSize;
    private int memoryCacheRetainTime;

    public void initialize(IEclipseContext context) {
        this.context = context;

        entryMap = new WeakValueHashMap<>();

        Application app = context.get(Application.class);

        rootDirectory = new File(app.getDataPath(), ROOT_PATH);
        try {
            IOUtils.createDirectory(rootDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

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

        undisposedImagesCleaner = new UndisposedImagesCleaner();

        backgroundSaver = new RepeatableRunner((int)(Math.random() * 1000*60*5), 1000*60*5,
                                               () -> storageManager.save());

        thumbnailSize = new Point(64, 64);
        memoryCacheRetainTime = 1000*60;
    }

    public void shutdown() {
        backgroundSaver.cancel();

        httpClient.cancelAll();

        for (ImageEntry entry : entryMap.values()) {
            entry.disposeImagesAll();
        }
        entryMap.clear();

        undisposedImagesCleaner.stop();

        storageManager.save();

        context = null;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    QueueThreadExecutor getStorageUpdateExecutor() {
        return storageUpdateExecutor;
    }

    public StandardHttpClient getHttpClient() {
        return httpClient;
    }

    public ImageEntry getEntry(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null)
            throw new IllegalArgumentException(url + " is invalid.");
        return getEntry(httpUrl);
    }

    public ImageEntry getEntry(HttpUrl url) {
        ImageEntry entry = entryMap.get(url);
        if (entry == null) {
            entry = new ImageEntry(this, url);
            entryMap.put(url, entry);
        }
        return entry;
    }

    public Map<HttpUrl, ImageEntry> getEntryMap() {
        return Collections.unmodifiableMap(entryMap);
    }

    public void setThumbnailSize(int width, int height) {
        thumbnailSize = new Point(width, height);
    }

    public Point getThumbnailSize() {
        return new Point(thumbnailSize.x, thumbnailSize.y);
    }

    public void setMemoryCacheRetainTime(int v) {
        memoryCacheRetainTime = v;
    }

    public int getMemoryCacheRetainTime() {
        return memoryCacheRetainTime;
    }

    UndisposedImagesCleaner getUndisposedImagesCleaner() {
        return undisposedImagesCleaner;
    }

    class UndisposedImagesCleaner {
        Map<HttpUrl, List<Image>> imagesMap;
        RepeatableRunner runner;

        UndisposedImagesCleaner() {
            imagesMap = new HashMap<>();

            runner = new RepeatableRunner(1000*60*5, 1000*60*5, () -> check());
        }

        void add(HttpUrl url, Image[] images) {
            List<Image> list = imagesMap.get(url);
            if (list == null) {
                list = new ArrayList<>();
                imagesMap.put(url, list);
            }
            Collections.addAll(list, images);
        }

        void check() {
            Set<HttpUrl> entryKeySet = getEntryMap().keySet();
            for (Map.Entry<HttpUrl, List<Image>> e : new ArrayList<>(imagesMap.entrySet())) {
                if (!entryKeySet.contains(e.getKey())) {
                    List<Image> list = e.getValue();
                    ImageUtils.disposeImages(list.toArray(new Image[list.size()]));
                }
            }
        }

        void stop() {
            runner.cancel();
        }
    }
}


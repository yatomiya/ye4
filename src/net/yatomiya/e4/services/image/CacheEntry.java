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
import org.eclipse.swt.graphics.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.util.*;

public class CacheEntry {
    ImageService service;
    HttpUrl url;
    StorageManager.EntryData eData;
    File storageFile;
    UpdateHandler updateHandler;

    class ImageTypeProcessor {
        ImageType type;
        Image[] images;
        List<CacheListener> listenerList;

        ImageTypeProcessor(ImageType type) {
            this.type = type;
            images = null;
            listenerList = new ArrayList<>();
        }

        ImageType getType() {
            return type;
        }

        Image[] getImages() {
            return images == null ? null : images.clone();
        }

        boolean hasImages() {
            return images != null && images.length > 0;
        }

        void allocateImages(ImageData[] imgData) {
            images = ImageUtils.allocateImages(imgData);
        }

        void disposeImages() {
            if (images != null) {
                ImageUtils.disposeImages(images);
                images = null;
            }
        }

        void subscribe(CacheListener listener) {
            if (listenerList.contains(listener))
                throw new IllegalStateException("listener is already registered.");
            listenerList.add(listener);
        }

        class DelayedCleaner implements Runnable, CacheListener {
            @Override
            public void run() {
                CacheEntry.this.getService().unsubscribe(CacheEntry.this, type, this);
                cleaner = null;
            }

            @Override
            public void handleEvent(CacheEvent event) {
            }
        }
        DelayedCleaner cleaner;

        void unsubscribe(CacheListener listener) {
            if (!listenerList.contains(listener))
                throw new IllegalStateException("listener is not registered.");
            listenerList.remove(listener);

            if (listenerList.size() == 0) {
                if (hasImages()) {
                    if (cleaner == null) {
                        cleaner = new DelayedCleaner();
                        subscribe(cleaner);
                        AppUtils.timerExec(cleaner, 1000*60);
                    } else {
                        disposeImages();
                    }
                }
            }
        }

        List<CacheListener> getCacheListeners() {
            return Collections.unmodifiableList(listenerList);
        }

        void fireCacheEvent(CacheEvent event) {
            for (CacheListener l : new ArrayList<>(listenerList)) {
                l.handleEvent(event);
            }
        }
    }

    Map<ImageType, ImageTypeProcessor> typeMap;

    CacheEntry(ImageService service, HttpUrl url) {
        this.service = service;
        this.url = url;
        this.eData = service.getStorageManager().getEntryData(url.toString());

        storageFile = service.getStorageManager().createImageStorageFile(eData.filename);

        {
            typeMap = new HashMap<>();

            typeMap.put(ImageType.ORIGINAL, new ImageTypeProcessor(ImageType.ORIGINAL));
            typeMap.put(ImageType.THUMBNAIL, new ImageTypeProcessor(ImageType.THUMBNAIL) {
                    @Override
                    void allocateImages(ImageData[] imgData) {
                        if (imgData == null)
                            throw new IllegalStateException();

                        ImageData[] thumbnailData = new ImageData[imgData.length];
                        for (int i = 0; i < imgData.length; i++) {
                            ImageData d = imgData[i];
                            int width = d.width;
                            int height = d.height;
                            Point thumbSize = new Point(64, 64);
                            int thumbWidth = thumbSize.x;
                            int thumbHeight = thumbSize.y;

                            Point newSize = ImageUtils.fitTo(width, height, thumbWidth, thumbHeight);
                            thumbnailData[i] = d.scaledTo(newSize.x, newSize.y);
                        }
                        images = ImageUtils.allocateImages(thumbnailData);
                    }
                });
        }
    }

    public ImageService getService() {
        return service;
    }

    void subscribe(ImageType type, CacheListener listener) {
        ImageTypeProcessor p = typeMap.get(type);
        p.subscribe(listener);
    }

    void unsubscribe(ImageType type, CacheListener listener) {
        ImageTypeProcessor p = typeMap.get(type);
        p.unsubscribe(listener);

        if (!hasCacheListener()) {
            if (isUpdating())
                cancel();
        }
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Image[] getImages(ImageType type) {
        eData.lastImageAccessTime = JUtils.getCurrentTime();

        ImageTypeProcessor p = typeMap.get(type);
        return p.getImages();
    }

    public boolean hasImages(ImageType type) {
        ImageTypeProcessor p = typeMap.get(type);
        return p.hasImages();
    }

    public File getStorageFile() {
        return storageFile;
    }

    public long getLastImageAccessTime() {
        return eData.lastImageAccessTime;
    }

    public long getLastNetworkAccessTime() {
        return eData.lastNetworkAccessTime;
    }

    public long getLastNetworkUpdateTime() {
        return eData.lastNetworkUpdateTime;
    }

    public CacheEvent getLastUpdateErrorEvent() {
        return eData.lastUpdateErrorEvent;
    }

    boolean hasCacheListener() {
        for (ImageTypeProcessor p : typeMap.values()) {
            if (p.getCacheListeners().size() > 0)
                return true;
        }
        return false;
    }

    public void update() {
        update(false);
    }

    public void update(boolean forceUpdate) {
        if (isUpdating())
            throw new IllegalStateException("Do not call update() when updating.");

        updateHandler = new UpdateHandler(this, forceUpdate);
        updateHandler.execute();
    }

    public void cancel() {
        if (updateHandler != null)
            updateHandler.cancel();
    }

    public boolean isUpdating() {
        return updateHandler != null;
    }

    public long getUpdatePriority() {
        if (updateHandler != null)
            return updateHandler.priority;
        return 0;
    }

    public void setUpdatePriority(long priority) {
        if (updateHandler != null)
            updateHandler.priority = priority;
    }

    void disposeImagesAll() {
        for (ImageTypeProcessor p : typeMap.values()) {
            p.disposeImages();
        }
    }

    void doneUpdate(CacheEvent event) {
        if (event.getType() == CacheEvent.Type.UPDATED) {
            disposeImagesAll();

            for (ImageTypeProcessor p : typeMap.values()) {
                if (p.getCacheListeners().size() > 0)
                    p.allocateImages(updateHandler.imgData);
            }
        }

        if (event.getType() == CacheEvent.Type.ERROR)
            eData.lastUpdateErrorEvent = event;

        fireCacheEvent(event);

        updateHandler = null;
    }

    void fireCacheEvent(CacheEvent event) {
        for (ImageTypeProcessor p : typeMap.values()) {
            p.fireCacheEvent(event);
        }
    }

}



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
import java.util.function.*;
import org.eclipse.swt.graphics.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.ui.image.*;
import net.yatomiya.e4.util.*;

public class ImageEntry {
    ImageService service;
    HttpUrl url;
    EntryData eData;
    String name;
    File storageFile;
    UpdateHandler updateHandler;

    class ImageTypeProcessor {
        ImageType type;
        Image[] images;
        AnimationMetaData animationMetaData;
        List<ImageListener> listenerList;

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

        void allocateImages(ImageLoader loader) {
            images = ImageUtils.allocateImages(loader);

            animationMetaData = new AnimationMetaData(updateHandler.loader);
        }

        void disposeImages() {
            if (images != null) {
                ImageUtils.disposeImages(images);
            }
            images = null;
            animationMetaData = null;
        }

        AnimationMetaData getAnimationMetaData() {
            return animationMetaData;
        }

        void subscribe(ImageListener listener) {
            if (listenerList.contains(listener))
                throw new IllegalStateException("listener is already registered.");
            listenerList.add(listener);
        }

        class DelayedImageDisposer implements Runnable, ImageListener {
            @Override
            public void run() {
                ImageEntry.this.unsubscribe(getType(), this);
                delayedDisposer = null;
            }

            @Override
            public void handleEvent(ImageEvent event) {
            }
        }
        DelayedImageDisposer delayedDisposer;

        void unsubscribe(ImageListener listener) {
            if (!listenerList.contains(listener))
                throw new IllegalStateException("listener is not registered.");
            listenerList.remove(listener);

            if (listenerList.size() == 0) {
                if (hasImages()) {
                    int retainTime = getService().getMemoryCacheRetainTime();
                    if (retainTime > 0 && delayedDisposer == null) {
                        delayedDisposer = new DelayedImageDisposer();
                        subscribe(delayedDisposer);
                        EUtils.timerExec(retainTime, delayedDisposer);
                    } else {
                        disposeImages();
                    }
                }
            }
        }

        List<ImageListener> getListeners() {
            return Collections.unmodifiableList(listenerList);
        }

        void fireCacheEvent(ImageEvent event) {
            for (ImageListener l : new ArrayList<>(listenerList)) {
                l.handleEvent(event);
            }
        }
    }

    Map<ImageType, ImageTypeProcessor> typeMap;

    ImageEntry(ImageService service, HttpUrl url) {
        this.service = service;
        this.url = url;

        eData = service.getStorageManager().getEntryData(url.toString());

        storageFile = service.getStorageManager().createImageStorageFile(eData.filename);

        {
            typeMap = new HashMap<>();

            typeMap.put(ImageType.ORIGINAL, new ImageTypeProcessor(ImageType.ORIGINAL));
            typeMap.put(ImageType.THUMBNAIL, new ImageTypeProcessor(ImageType.THUMBNAIL) {
                    @Override
                    void allocateImages(ImageLoader loader) {
                        animationMetaData = new AnimationMetaData(loader);

                        int width = loader.logicalScreenWidth;
                        int height = loader.logicalScreenHeight;
                        if (width == 0 || height == 0) {
                            width = loader.data[0].width;
                            height = loader.data[0].height;
                        }

                        Point thumbSize = getService().getThumbnailSize();
                        if (width < thumbSize.x && height < thumbSize.y) {
                            ImageTypeProcessor p = typeMap.get(ImageType.ORIGINAL);
                            if (!p.hasImages()) {
                                p.allocateImages(loader);
                            }
                            images = p.getImages();
                        } else {
                            Point newSize = ImageUtils.fitTo(width, height, thumbSize.x, thumbSize.y);
                            images = ImageUtils.allocateImages(loader, newSize.x, newSize.y);
                        }
                    }
                });
        }
    }

    public ImageService getService() {
        return service;
    }

    public void subscribe(ImageType type, ImageListener listener) {
        subscribe(type, listener, true);
    }

    public void subscribe(ImageType type, ImageListener listener, boolean doUpdate) {
        ImageTypeProcessor p = typeMap.get(type);
        p.subscribe(listener);

        if (doUpdate && !hasImages(type) && !isUpdating()) {
            update();
        }
    }

    public void unsubscribe(ImageType type, ImageListener listener) {
        ImageTypeProcessor p = typeMap.get(type);
        p.unsubscribe(listener);

        if (!hasListener()) {
            if (isUpdating())
                cancel();
        }
    }

    public HttpUrl getUrl() {
        return url;
    }

    public EntryData getEntryData() {
        return eData;
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

    public boolean hasImages() {
        return CUtils.anyMatch(CUtils.toList(ImageType.values()), type -> hasImages(type));
    }

    public AnimationMetaData getAnimationMetaData(ImageType type) {
        return typeMap.get(type).getAnimationMetaData();
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

    public ImageEvent getLastUpdateErrorEvent() {
        return eData.lastUpdateErrorEvent;
    }

    boolean hasListener() {
        for (ImageTypeProcessor p : typeMap.values()) {
            if (p.getListeners().size() > 0)
                return true;
        }
        return false;
    }

    public void update() {
        update(false);
    }

    public void update(boolean forceNetworkUpdate) {
        if (isUpdating())
            return;

        updateHandler = new UpdateHandler(this, forceNetworkUpdate);
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

    void doneUpdate(ImageEvent event) {
        if (event.getType() == ImageEvent.Type.UPDATED) {
            disposeImagesAll();

            for (ImageTypeProcessor p : typeMap.values()) {
                if (p.getListeners().size() > 0) {
                    p.allocateImages(updateHandler.loader);

                    getService().getUndisposedImagesCleaner().add(getUrl(), p.getImages());
                }
            }
        }

        if (event.getType() == ImageEvent.Type.ERROR)
            eData.lastUpdateErrorEvent = event;

        fireCacheEvent(event);

        updateHandler = null;
    }

    void fireCacheEvent(ImageEvent event) {
        for (ImageTypeProcessor p : typeMap.values()) {
            p.fireCacheEvent(event);
        }
    }

    public FramePlayer createAnimationPlayer(ImageType type, IntConsumer activateFunc) {
        AnimationMetaData animData = getAnimationMetaData(type);
        if (animData == null)
            return null;

        FramePlayer player = new FramePlayer(animData.dataCount, animData.repeatCount) {
                @Override
                protected void frameActivated(int frameIndex) {
                    activateFunc.accept(frameIndex);
                }

                @Override
                protected int getFrameInterval(int frameIndex) {
                    return animData.delayTime[frameIndex];
                }
            };
        return player;
    }
}


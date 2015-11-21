/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.ui.util.*;

public class ImageCanvas extends Canvas {
    ImageService imageService;
    CacheEntry entry;
    ImageType imageType;
    CacheListener cacheListener;
    UIResourceManager resMgr;

    boolean isFitToSize;
    float scaleX, scaleY;

    public ImageCanvas(Composite parent, int style, String url, ImageType imageType) {
        super(parent, style);

        addPaintListener(event -> doPaint(event));

        imageService = AppUtils.get(ImageService.class);
        this.imageType = imageType;
        cacheListener = event -> handleImageEvent(event);
        entry = imageService.subscribe(url, imageType, cacheListener);
        resMgr = new UIResourceManager(this);

        addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent event) {
                    imageService.unsubscribe(entry, imageType, cacheListener);
                }
            });

        isFitToSize = false;
        scaleX = scaleY = 1.0f;
    }

    protected void handleImageEvent(CacheEvent event) {
        redraw();
    }

    void doPaint(PaintEvent event) {
        Point size = getSize();
        Rectangle drawRect = new Rectangle(0, 0, size.x, size.y);

        if (entry.hasImages(imageType)) {
            Image[] images = entry.getImages(imageType);
            Rectangle imgRect = images[0].getBounds();

            if (isFitToSize) {
                drawRect = ImageUtils.fitToCenter(imgRect, drawRect);
            }

            ImageUtils.draw(event.gc, entry, imageType, imgRect, drawRect);
        } else {
            ImageUtils.drawImageCacheInformation(event.gc, entry, resMgr, drawRect);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        if (!isFitToSize) {
            if (entry.hasImages(imageType)) {
                Image[] images = entry.getImages(imageType);
                Rectangle bounds = images[0].getBounds();

                bounds.width *= scaleX;
                bounds.height *= scaleY;

                return new Point(bounds.width, bounds.height);
            }
        }
        return new Point(wHint, hHint);
    }

    public void setScale(float x, float y) {
        if (scaleX != x || scaleY != y) {
            scaleX = x;
            scaleY = y;
            redraw();
        }
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setFitToSize(boolean v) {
        if (isFitToSize != v) {
            isFitToSize = v;
            redraw();
        }
    }

    public boolean isFitToSize() {
        return isFitToSize;
    }
}


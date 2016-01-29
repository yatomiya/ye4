/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.image;

import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.util.*;

public class ImageCanvas extends Canvas {
    ImageService imageService;
    ImageEntry entry;
    ImageType imageType;
    ImageListener imageListener;
    UIResourceManager resMgr;

    boolean isFitToSize;
    float scaleX, scaleY;

    FramePlayer player;

    public ImageCanvas(Composite parent, int style, String url, ImageType imageType) {
        super(parent, style);

        addPaintListener(event -> doPaint(event));

        imageService = EUtils.get(ImageService.class);
        this.imageType = imageType;
        imageListener = event -> handleImageEvent(event);
        entry = imageService.getEntry(url);
        entry.subscribe(imageType, imageListener);

        addDisposeListener(event -> widgetDisposed());

        resMgr = new UIResourceManager(this);

        isFitToSize = false;
        scaleX = scaleY = 1.0f;

        activatePlayer();
    }

    void widgetDisposed() {
        deactivatePlayer();
        entry.unsubscribe(imageType, imageListener);
    }

    protected void handleImageEvent(ImageEvent event) {
        if (event.getType() == ImageEvent.Type.UPDATED) {
            activatePlayer();
        } else {
            if (player != null)
                player.stop();
        }

        redraw();
    }

    void activatePlayer() {
        if (player != null)
            return;

        player = entry.createAnimationPlayer(imageType, index -> redraw());
        if (player == null)
            return;

        player.play();
    }

    void deactivatePlayer() {
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    void doPaint(PaintEvent event) {
        Point size = getSize();
        Rectangle drawRect = new Rectangle(0, 0, size.x, size.y);

        if (player != null) {
            Image image = entry.getImages(imageType)[player.getFrameIndex()];

            Rectangle imgRect = image.getBounds();
            if (isFitToSize) {
                drawRect = ImageUtils.fitToCenter(imgRect, drawRect);
            }

            event.gc.drawImage(image, imgRect.x, imgRect.y, imgRect.width, imgRect.height,
                               drawRect.x, drawRect.y, drawRect.width, drawRect.height);
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

    public FramePlayer getPlayer() {
        return player;
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


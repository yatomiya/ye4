/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.image;

import net.yatomiya.e4.util.*;

public class EntryData {
    String url;
    String filename;
    long fileByteSize;
    long lastImageAccessTime;
    long lastNetworkAccessTime;
    long lastNetworkUpdateTime;
    ImageEvent lastUpdateErrorEvent;

    EntryData(String url, String filename) {
        this.url = url;
        this.filename = filename;
        fileByteSize = -1;

        long currentTime = JUtils.getCurrentTime();
        lastImageAccessTime = currentTime;
        lastNetworkAccessTime = currentTime;
        lastNetworkUpdateTime = currentTime;

        lastUpdateErrorEvent = null;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public long getFileByteSize() {
        return fileByteSize;
    }

    public long getLastImageAccessTime() {
        return lastImageAccessTime;
    }

    public long getLastNetworkAccessTime() {
        return lastNetworkAccessTime;
    }

    public long getLastNetworkUpdateTime() {
        return lastNetworkUpdateTime;
    }

    public ImageEvent getLastUpdateErrorEvent() {
        return lastUpdateErrorEvent;
    }
}


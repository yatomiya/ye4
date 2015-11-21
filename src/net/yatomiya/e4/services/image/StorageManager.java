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
import java.util.stream.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.util.*;

class StorageManager {
    static final String STORAGE_PATH = "cache";

    ImageService service;
    File storageRootDirectory;
    File persistenceFile;

    static class EntryData {
        String url = "";
        String filename = "";
        long fileByteSize = -1;
        long lastImageAccessTime = -1;
        long lastNetworkAccessTime = -1;
        long lastNetworkUpdateTime = -1;
        CacheEvent lastUpdateErrorEvent;
    }

    static class PersistentData {
        long maxStorageByteSize = 1024*1024*1024;
        long maxFileCount = 65535;
        List<EntryData> entryList = new ArrayList<>();
    }

    PersistentData pData;

    StorageManager(ImageService service) {
        this.service = service;

        storageRootDirectory = new File(service.getRootDirectory(), STORAGE_PATH);
        IOUtils.checkAndCreateDirectory(storageRootDirectory);

        persistenceFile = new File(storageRootDirectory, "storage.ser");
    }

    void load() {
        try {
            pData = (PersistentData)IOUtils.readObject(persistenceFile);
        } catch (IOException e) {
            pData = new PersistentData();
        }
    }

    void save() {
        cleanupByStorageSize();

        try {
            IOUtils.writeObject(persistenceFile, pData);
        } catch (IOException e) {
        }
    }

    EntryData getEntryData(String url) {
        EntryData data = CUtils.findFirst(pData.entryList, d -> url.equals(d.url));
        if (data == null) {
            data= new EntryData();

            data.url = url;

            data.filename = StringUtils.digest(url);
            String ext = HttpUtils.getPathExtension(HttpUrl.parse(url));
            if (JUtils.isNotEmpty(ext))
                data.filename += "." + ext;

            pData.entryList.add(data);
        }
        return data;
    }

    File createImageStorageFile(String filename) {
        return new File(storageRootDirectory, filename);
    }

    void cleanupByStorageSize() {
        Map<HttpUrl, CacheEntry> entryMap = service.getCacheEntryMap();
        List<EntryData> list = pData.entryList.stream().
            filter(d -> entryMap.get(HttpUrl.parse(d.url)) == null).      // exclude entry in use
            sorted((d1, d2) -> JUtils.sign(d1.lastImageAccessTime - d2.lastImageAccessTime)).  // recently used first
            collect(Collectors.toList());

        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            EntryData d = list.get(i);
            if (d.fileByteSize < 0) {
                pData.entryList.remove(d);
            } else {
                if (i > pData.maxFileCount
                    || total > pData.maxStorageByteSize) {
                    File file = createImageStorageFile(d.filename);
                    file.delete();
                    pData.entryList.remove(d);
                }
                total += d.fileByteSize;
            }
        }
    }
}


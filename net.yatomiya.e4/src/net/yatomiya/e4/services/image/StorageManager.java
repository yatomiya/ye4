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

public class StorageManager {
    static final String STORAGE_PATH = "cache";

    ImageService service;
    File storageRootDirectory;
    File persistenceFile;

    static class PersistentData {
        long maxStorageByteSize = 1024*1024*1024;
        long maxFileCount = 65535;
        List<EntryData> entryList = new ArrayList<>();
    }

    PersistentData pData;

    StorageManager(ImageService service) {
        this.service = service;

        storageRootDirectory = new File(service.getRootDirectory(), STORAGE_PATH);
        try {
            IOUtils.createDirectory(storageRootDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        persistenceFile = new File(service.getRootDirectory(), "storage.ser");
    }

    public void load() {
        try {
            pData = (PersistentData)IOUtils.readObject(persistenceFile);
        } catch (IOException e) {
            pData = new PersistentData();
        }
    }

    public void save() {
        cleanup();

        try {
            IOUtils.writeObject(persistenceFile, pData);
        } catch (IOException e) {
        }
    }

    public EntryData getEntryData(String url) {
        EntryData data = CUtils.findFirst(pData.entryList, d -> url.equals(d.url));
        if (data == null) {
            String filename = StringUtils.digest(url);
            String ext = StringUtils.getExtension(HttpUtils.getPathName(HttpUrl.parse(url)));
            if (!JUtils.isEmpty(ext))
                filename += "." + ext;

            data= new EntryData(url, filename);

            pData.entryList.add(data);
        }
        return data;
    }

    File getImageStorageFile(String filename) {
        return new File(storageRootDirectory, filename);
    }

    public void cleanup() {
        Map<HttpUrl, ImageEntry> entryMap = service.getEntryMap();
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
                    File file = getImageStorageFile(d.filename);
                    file.delete();
                    pData.entryList.remove(d);
                }
                total += d.fileByteSize;
            }
        }
    }

    public void setMaxStorageByteSize(long v) {
        pData.maxStorageByteSize = v;
    }

    public long getMaxStorageByteSize() {
        return pData.maxStorageByteSize;
    }
}



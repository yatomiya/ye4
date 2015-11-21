/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.image;

import java.io.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.squareup.okhttp.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;

class UpdateHandler implements Callback, Comparable<UpdateHandler> {
    CacheEntry entry;
    boolean isCanceled;
    StorageTask storageTask;
    Call httpCall;
    boolean forceUpdate;
    long priority;
    CacheEvent event;
    ImageData[] imgData;

    class StorageTask implements Runnable, Comparable<StorageTask> {
        UpdateHandler handler;

        void execute() {
            entry.getService().getStorageUpdateExecutor().execute(this);
        }

        @Override
        public void run() {
            if (isCanceled) {
                doneUpdate(CacheEvent.createCanceled(entry));
                return;
            }

            if (!forceUpdate && IOUtils.isFileExists(entry.getStorageFile())) {
                try {
                    synchronized (fileAccessLock) {
                        imgData = new ImageLoader().load(new BufferedInputStream(new FileInputStream(entry.getStorageFile())));
                    }

                    doneUpdate(CacheEvent.createUpdated(entry, CacheEvent.Source.STORAGE));
                    return;
                } catch (SWTException | IOException e) {
                    // try network update
                    imgData = null;
                }
            }

            StandardHttpClient client = entry.getService().getHttpClient();
            Request.Builder builder = new Request.Builder().url(entry.getUrl());
            builder.tag(entry);
            Request request = builder.build();
            httpCall = client.execute(request, UpdateHandler.this, false);
        }

        @Override
        public int compareTo(StorageTask obj) {
            return handler.compareTo(obj.handler);
        }
    }

    // 同時に多数のスレッドからファイルアクセスが起こると処理落ちするので、ファイルアクセスは１スレッドに制限する。
    static Object fileAccessLock = new Object();

    UpdateHandler(CacheEntry entry, boolean forceUpdate) {
        this.entry = entry;
        this.forceUpdate = forceUpdate;
        isCanceled = false;
        priority = JUtils.getCurrentTime();
    }

    void execute() {
        storageTask = new StorageTask();
        storageTask.handler = this;
        storageTask.execute();
    }

    void cancel() {
        isCanceled = true;
        if (httpCall != null)
            httpCall.cancel();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (isCanceled) {
            doneUpdate(CacheEvent.createCanceled(entry));
            return;
        }

        entry.eData.lastNetworkAccessTime = JUtils.getCurrentTime();

        if (response.code() == 200) {
            // OK
            byte[] buf = null;
            buf = response.body().bytes();

            if (buf != null) {

                File file = entry.getStorageFile();
                IOUtils.write(file, buf);
                entry.eData.fileByteSize = file.length();

                try {
                    imgData = new ImageLoader().load(new ByteArrayInputStream(buf));

                    entry.eData.lastNetworkUpdateTime = JUtils.getCurrentTime();

                    event = CacheEvent.createUpdated(entry, CacheEvent.Source.NETWORK);
                } catch (SWTException e) {
                    event = CacheEvent.imageLoadingError(entry, CacheEvent.Source.NETWORK, e);
                }
            }
        } else {
            event = CacheEvent.createHttpStatusCodeError(entry, response.code());
        }

        doneUpdate(event);
    }

    @Override
    public void onFailure(Request request, IOException e) {
        if (httpCall.isCanceled()) {
            doneUpdate(CacheEvent.createCanceled(entry));
        } else {
            doneUpdate(CacheEvent.createExceptionError(entry, CacheEvent.Source.NETWORK, CacheEvent.ErrorType.IO_EXCEPTION, e));
        }
    }

    void doneUpdate(CacheEvent event) {
        AppUtils.syncExec(() -> entry.doneUpdate(event));

        storageTask = null;
        httpCall = null;
    }

    @Override
    public int compareTo(UpdateHandler obj) {
        return JUtils.sign(priority - obj.priority);
    }
}



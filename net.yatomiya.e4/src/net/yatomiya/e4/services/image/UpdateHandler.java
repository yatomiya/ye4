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
import net.yatomiya.e4.util.*;

class UpdateHandler implements Callback, Comparable<UpdateHandler> {
    ImageEntry entry;
    boolean isCanceled;
    StorageTask storageTask;
    Call httpCall;
    boolean forceNetworkUpdate;
    long priority;
    ImageEvent event;
    ImageLoader loader;

    class StorageTask implements Runnable, Comparable<StorageTask> {
        UpdateHandler handler;

        void execute() {
            entry.getService().getStorageUpdateExecutor().execute(this);
        }

        @Override
        public void run() {
            if (isCanceled) {
                doneUpdate(ImageEvent.createCanceled(entry));
                return;
            }

            if (!forceNetworkUpdate && IOUtils.isFileExists(entry.getStorageFile())) {
                try {
                    synchronized (fileAccessLock) {
                        loader = new ImageLoader();
                        loader.load(new BufferedInputStream(new FileInputStream(entry.getStorageFile())));
                    }

                    if (loader.data == null || loader.data.length <= 0) {
                        throw new SWTException(SWT.ERROR_INVALID_IMAGE, "Loading is successfull but no image in file.");
                    }

                    doneUpdate(ImageEvent.createUpdated(entry, ImageEvent.Source.STORAGE));
                } catch (SWTException e) {
                    loader = null;
                    doneUpdate(ImageEvent.createSWTExceptionError(entry, ImageEvent.Source.STORAGE, e));
                } catch (IOException e) {
                    loader = null;
                    doneUpdate(ImageEvent.createExceptionError(entry, ImageEvent.Source.STORAGE, ImageEvent.ErrorType.IO_EXCEPTION, e));
                }
            } else {
                StandardHttpClient client = entry.getService().getHttpClient();
                Request.Builder builder = new Request.Builder().url(entry.getUrl());
                builder.tag(entry);
                Request request = builder.build();
                httpCall = client.execute(request, UpdateHandler.this, false);
            }
        }

        @Override
        public int compareTo(StorageTask obj) {
            return handler.compareTo(obj.handler);
        }
    }

    // ファイルアクセスは１スレッドに制限する。
    static Object fileAccessLock = new Object();

    UpdateHandler(ImageEntry entry, boolean forceNetworkUpdate) {
        this.entry = entry;
        this.forceNetworkUpdate = forceNetworkUpdate;
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
            doneUpdate(ImageEvent.createCanceled(entry));
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
                    loader = new ImageLoader();
                    loader.load(new ByteArrayInputStream(buf));

                    if (loader.data == null || loader.data.length <= 0) {
                        throw new SWTException(SWT.ERROR_INVALID_IMAGE, "Loading is successfull but no image in file.");
                    }

                    entry.eData.lastNetworkUpdateTime = JUtils.getCurrentTime();

                    event = ImageEvent.createUpdated(entry, ImageEvent.Source.NETWORK);
                } catch (SWTException e) {
                    event = ImageEvent.createSWTExceptionError(entry, ImageEvent.Source.NETWORK, e);
                }
            }
        } else {
            event = ImageEvent.createHttpStatusCodeError(entry, response.code());
        }

        doneUpdate(event);
    }

    @Override
    public void onFailure(Request request, IOException e) {
        if (httpCall.isCanceled()) {
            doneUpdate(ImageEvent.createCanceled(entry));
        } else {
            doneUpdate(ImageEvent.createExceptionError(entry, ImageEvent.Source.NETWORK, ImageEvent.ErrorType.IO_EXCEPTION, e));
        }
    }

    void doneUpdate(ImageEvent event) {
        EUtils.syncExec(() -> entry.doneUpdate(event));

        storageTask = null;
        httpCall = null;
    }

    @Override
    public int compareTo(UpdateHandler obj) {
        return JUtils.sign(priority - obj.priority);
    }
}



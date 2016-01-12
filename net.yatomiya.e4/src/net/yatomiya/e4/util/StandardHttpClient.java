/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import com.squareup.okhttp.*;

public class StandardHttpClient extends OkHttpClient {
    public Call execute(Request request, Callback callback, boolean isSynchronous) {
        Call call = newCall(request);
        if (isSynchronous) {
            try {
                Response response = call.execute();
                callback.onResponse(response);
            } catch (IOException e) {
                callback.onFailure(request, e);
            }
        } else {
            call.enqueue(callback);
        }
        return call;
    }

    public void cancelAll() {
        getDispatcher().cancel(new Object() {
                @Override
                public boolean equals(Object o) {
                    return true;
                }
            });
    }

    public boolean isCallExecuting() {
        return getDispatcher().getQueuedCallCount() > 0
            || getDispatcher().getRunningCallCount() > 0;
    }

    public Call download(String url, boolean isSynchronous, Consumer<Response> responseHandler) {
        return download(url, null, isSynchronous, responseHandler);
    }

    public Call download(String url, Date lastModifiedSince, boolean isSynchronous, Consumer<Response> responseHandler) {
        Request request = HttpUtils.createRequestBuilder(HttpUrl.parse(url), lastModifiedSince).build();
        Callback callback = new Callback() {
                @Override
                public void onResponse(Response response) throws IOException {
                    responseHandler.accept(response);
                }

                @Override
                public void onFailure(Request request, IOException e) {
                    responseHandler.accept(null);
                }
            };
        return execute(request, callback, isSynchronous);
    }

    public byte[] download(String url) {
        Object[] buf = new Object[1];
        download(url, true, response ->  {
                try {
                    buf[0] = response.body().bytes();
                } catch (IOException e) {
                    buf[0] = null;
                }
            });
        return (byte[])buf[0];
    }

    public static class CallComparator<T extends Callback> implements Comparator<Runnable> {
        Class<Runnable> asyncCallClass;
        Field responseCallbackField;
        Comparator<T> func;

        public CallComparator(Comparator<T> func) {
            this.func = func;
        }

        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (asyncCallClass == null) {
                asyncCallClass = (Class<Runnable>)o1.getClass();
                try {
                    responseCallbackField = asyncCallClass.getDeclaredField("responseCallback");
                    responseCallbackField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException(e);
                }
            }
            try {
                T handler1 = (T)responseCallbackField.get(o1);
                T handler2 = (T)responseCallbackField.get(o2);
                return func.compare(handler1, handler2);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}



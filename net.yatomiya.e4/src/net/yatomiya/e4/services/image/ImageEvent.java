/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.image;

import org.eclipse.swt.*;

public class ImageEvent {
    public enum Type {
        UPDATED,
        ERROR,
        CANCELED,
    }

    public enum Source {
        MEMORY,
        STORAGE,
        NETWORK,
    }

    public enum ErrorType {
        IO_EXCEPTION,
        HTTP_STATUS_CODE,
        UNSUPPORTED_IMAGE_FORMAT,
    }

    private transient ImageEntry entry;
    private Type type;
    private Source source;
    private ErrorType errorType;
    private Throwable exception;
    private int httpStatusCode;

    private ImageEvent(ImageEntry entry,
                              Type type,
                              Source source,
                              ErrorType errorType,
                              Throwable exception,
                              int httpStatusCode
        ) {
        this.entry = entry;
        this.type = type;
        this.source = source;
        this.errorType = errorType;
        this.exception = exception;
        this.httpStatusCode = httpStatusCode;
    }

    static ImageEvent createUpdated(ImageEntry entry, Source source) {
        return new ImageEvent(entry, Type.UPDATED, source, null, null, -1);
    }

    static ImageEvent createExceptionError(ImageEntry entry, Source source, ErrorType errorType, Throwable exception) {
        return new ImageEvent(entry, Type.ERROR, source, errorType, exception, -1);
    }

    static ImageEvent createHttpStatusCodeError(ImageEntry entry, int httpStatusCode) {
        return new ImageEvent(entry, Type.ERROR, Source.NETWORK, ErrorType.HTTP_STATUS_CODE, null, httpStatusCode);
    }

    static ImageEvent createSWTExceptionError(ImageEntry entry, Source source, SWTException e) {
        ErrorType error;
        int code = e.code;
        if (code == SWT.ERROR_IO) {
            error = ErrorType.IO_EXCEPTION;
        } else if (code == SWT.ERROR_INVALID_IMAGE
                   || code == SWT.ERROR_UNSUPPORTED_FORMAT) {
            error = ErrorType.UNSUPPORTED_IMAGE_FORMAT;
        } else {
            error = ErrorType.IO_EXCEPTION;
        }
        return new ImageEvent(entry, Type.ERROR, source, error, e, -1);
    }

    static ImageEvent createCanceled(ImageEntry entry) {
        return new ImageEvent(entry, Type.CANCELED, null, null, null, -1);
    }

    public ImageEntry getCacheEntry() {
        return entry;
    }

    public Type getType() {
        return type;
    }

    public Source getSource() {
        return source;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Throwable getException() {
        return exception;
    }
}

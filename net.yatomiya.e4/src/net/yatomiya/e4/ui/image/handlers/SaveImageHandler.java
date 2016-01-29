/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.image.handlers;

import java.io.*;
import javax.inject.*;
import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.e4.ui.services.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.ui.dialogs.*;
import net.yatomiya.e4.ui.image.*;
import net.yatomiya.e4.util.*;

public class SaveImageHandler {
    @CanExecute
    public boolean canExecute(ImageService imageService, @Optional @Named(ImageUtils.HANDLER_IMAGE_URL) String url) {
        if (JUtils.isEmpty(url))
            return false;

        ImageEntry entry = imageService.getEntry(url);
        if (entry == null)
            return false;

        if (entry.isUpdating())
            return false;

        File storageFile = entry.getStorageFile();
        return IOUtils.isFileExists(storageFile);
    }

    @Execute
    public void execute(ImageService imageService,
                        @Optional @Named(ImageUtils.HANDLER_IMAGE_URL) String url,
                        @Named(IServiceConstants.ACTIVE_SHELL) Shell shell
        ) {
        if (!canExecute(imageService, url))
            return;

        ImageEntry entry = imageService.getEntry(url);

        String name = HttpUtils.getPathName(entry.getUrl());

        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        dlg.setFileName(name);
        String path = dlg.open();
        if (path != null) {
            try {
                IOUtils.copy(entry.getStorageFile(), new File(path));
            } catch (IOException e) {
                DialogUtils.openError(null, "", path + " の保存に失敗しました。");
            }
        }
    }
}

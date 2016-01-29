/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.image.handlers;

import javax.inject.*;
import org.eclipse.e4.core.di.annotations.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.ui.image.*;
import net.yatomiya.e4.util.*;

public class ReloadImageHandler {
    @CanExecute
    public boolean canExecute(ImageService imageService, @Optional @Named(ImageUtils.HANDLER_IMAGE_URL) String url) {
        if (JUtils.isEmpty(url))
            return false;

        ImageEntry entry = imageService.getEntry(url);
        if (entry == null)
            return false;

        if (entry.isUpdating())
            return false;

        return true;
    }

    @Execute
    public void execute(ImageService imageService, @Named(ImageUtils.HANDLER_IMAGE_URL) String url) {
        ImageEntry entry = imageService.getEntry(url);
        if (entry == null || entry.isUpdating())
            return;

        entry.update(true);
    }
}

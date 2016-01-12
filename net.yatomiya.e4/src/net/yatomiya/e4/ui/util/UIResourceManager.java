/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.net.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.util.*;

public class UIResourceManager implements ISharedTextColors {
    LocalResourceManager resMgr;

    public UIResourceManager() {
        resMgr = new LocalResourceManager(JFaceResources.getResources());
    }

    public UIResourceManager(Control disposer) {
        resMgr = new LocalResourceManager(JFaceResources.getResources(), disposer);
    }

    @Override
    public void dispose() {
        resMgr.dispose();
    }

    @Override
    public Color getColor(RGB rgb) {
        return resMgr.createColor(
            ColorDescriptor.createFrom(rgb));
    }

    public Color getColor(int r, int g, int b) {
        return resMgr.createColor(ColorDescriptor.createFrom(new RGB(r, g, b)));
    }

    // "128,0,513"
    public Color getColor(String colorString) {
        String[] v = StringUtils.split(colorString, ",");
        int r = 0;
        int g = 0;
        int b = 0;
        if (v.length < 3)
            throw new IllegalArgumentException();

        r = Integer.valueOf(v[0]);
        g = Integer.valueOf(v[1]);
        b = Integer.valueOf(v[2]);
        return getColor(r, g, b);
    }

    public Font getFont(String name, int height, int style) {
        return resMgr.createFont(
            FontDescriptor.createFrom(name, height, style));
    }

    public Font getFont(FontData data) {
        return resMgr.createFont(FontDescriptor.createFrom(data));
    }

    public Font getFont(String fontDataString) {
        return getFont(new FontData(fontDataString));
    }

    public Image getImage(String urlStr) {
        URL url = HttpUtils.toURL(urlStr);
        if (url != null)
            return resMgr.createImage(ImageDescriptor.createFromURL(url));
        return null;
    }
}



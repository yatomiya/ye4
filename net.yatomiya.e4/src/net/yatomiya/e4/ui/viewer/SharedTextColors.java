/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import org.eclipse.jface.text.source.*;
import org.eclipse.swt.graphics.*;
import net.yatomiya.e4.ui.util.*;

public class SharedTextColors implements ISharedTextColors {
    UIResourceManager mgr = new UIResourceManager();

    @Override
    public Color getColor(RGB rgb) {
        return mgr.getColor(rgb);
    }

    @Override
    public void dispose() {
        mgr.dispose();
    }
}

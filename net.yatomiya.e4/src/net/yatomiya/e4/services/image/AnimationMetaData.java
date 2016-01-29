/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.image;

import org.eclipse.swt.graphics.*;

public class AnimationMetaData {
    public int dataCount;
    public int repeatCount;
    public int[] delayTime;

    AnimationMetaData(ImageLoader loader) {
        dataCount = loader.data.length;
        repeatCount = loader.repeatCount;

        delayTime = new int[loader.data.length];
        for (int i = 0; i < delayTime.length; i++) {
            // gif delay time is milli second.
            // convert to micro second.
            delayTime[i] = loader.data[i].delayTime * 10;
        }
    }
}


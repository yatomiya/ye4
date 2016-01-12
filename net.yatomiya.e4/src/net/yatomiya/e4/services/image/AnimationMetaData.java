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


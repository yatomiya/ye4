/*******************************************************************************
 * Copyright (c) 2016 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

public abstract class FramePlayer {
    int frameCount;
    int maxLoopCount;
    int frameIndex;
    boolean isPlaying;
    int loopCount;

    class Activator implements Runnable {
        boolean isCancel = false;

        @Override
        public void run() {
            if (isCancel)
                return;

            frameActivated(frameIndex);

            // in case stop() is called in frameActivated().
            if (!isCancel) {
                int interval = getFrameInterval(frameIndex);
                if (interval > 0) {
                    frameIndex += 1;
                    if (frameIndex >= getFrameCount()) {
                        frameIndex = 0;
                        loopCount += 1;
                    }

                    if (maxLoopCount <= 0
                        || (maxLoopCount > 0 && loopCount < maxLoopCount)) {
                        EUtils.timerExec(interval, activator);
                    } else {
                        stop();
                    }
                }
            }
        }
    }
    Activator activator;

    public FramePlayer(int frameCount, int maxLoopCount) {
        if (frameCount <= 0)
            throw new IllegalArgumentException("frame count must be greater than 0.");

        this.frameCount = frameCount;
        this.maxLoopCount = maxLoopCount;
        isPlaying = false;
        frameIndex = 0;
        activator = null;
    }

    protected abstract void frameActivated(int frameIndex);

    protected abstract int getFrameInterval(int frameIndex);

    public int getFrameCount() {
        return frameCount;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int v) {
        if (v < 0 || frameCount <= 0) {
            throw new IllegalArgumentException();
        }

        frameIndex = v;
    }

    public int getMaxLoopCount() {
        return maxLoopCount;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void play() {
        if (!isPlaying) {
            isPlaying = true;

            activator = new Activator();
            activator.run();
        }
    }

    public void stop() {
        if (isPlaying) {
            isPlaying = false;

            activator.isCancel = true;
            activator = null;
        }
    }

    public void rewind() {
        frameIndex = 0;
        loopCount = 0;

        if (isPlaying()) {
            stop();
            play();
        }
    }

}

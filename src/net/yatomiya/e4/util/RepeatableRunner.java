/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import net.yatomiya.e4.*;

public abstract class RepeatableRunner implements Runnable {
    private int startInterval;
    private int execInterval;
    private boolean isCanceled;

    public RepeatableRunner(int startInterval, int execInterval) {
        this.startInterval = startInterval;
        this.execInterval = execInterval;
        isCanceled = false;
    }

    public void start() {
        Runnable runner = new Runnable() {
                @Override
                public void run() {
                    if (!isCanceled) {
                        RepeatableRunner.this.run();
                        AppUtils.timerExec(this, execInterval);
                    }
                }
            };
        AppUtils.timerExec(runner, startInterval);
    }

    @Override
    public abstract void run();

    public void cancel() {
        isCanceled = true;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public int getExecInterval() {
        return execInterval;
    }

    public void setExecInterval(int execInterval) {
        this.execInterval = execInterval;
    }
}

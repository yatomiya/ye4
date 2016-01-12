/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;
import java.util.concurrent.*;


/**
 * Use only execute(FutureTask) for execute method call.
 */
public class QueueThreadExecutor extends ThreadPoolExecutor {

    /**
     * - ThreadPoolExecutor core size と maximum size について。
     * core を越えるタスクを execute() すると、そのタスクはキューに保存される。そして「キューがいっぱいになったときに」スレッドが
     * maximum まで生成される。
     * coreを越えたときにmaximumまでスレッドが増えていき、maximumに到達するとキューに貯めていく、ではない。
     */

    private List<FutureTask> executingList;

    public QueueThreadExecutor(int poolSize, Comparator<? super Runnable> comparator) {
        super(poolSize, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
              comparator != null ? new PriorityBlockingQueue<Runnable>(32, comparator) : new LinkedBlockingQueue());

        executingList = new ArrayList<>();
    }

    public QueueThreadExecutor(int poolSize) {
        this(poolSize, null);
    }

    public void execute(FutureTask command) {
        checkDone();
        executingList.add(command);

        super.execute(command);
    }

    protected void checkDone() {
        for (FutureTask task : new ArrayList<>(executingList)) {
            if (task.isDone()) {
                executingList.remove(task);
            }
        }
    }

    public FutureTask[] getExecutingTasks() {
        checkDone();
        return executingList.toArray(new FutureTask[executingList.size()]);
    }

    public int getExecutingTaskCount() {
        checkDone();
        return executingList.size();
    }

    public void awaitDoneAll() throws InterruptedException {
        while (getExecutingTaskCount() > 0) {
            Thread.sleep(10);
        }
    }

    public void cancelTaskAll() {
        checkDone();
        for (FutureTask task : executingList.toArray(new FutureTask[executingList.size()])) {
            task.cancel(true);
        }
    }
}

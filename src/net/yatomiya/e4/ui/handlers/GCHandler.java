/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.jface.dialogs.*;



public class GCHandler {
    @Execute
    public void execute() {
        System.gc();

        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory()/(1024*1024);
        long total = rt.totalMemory()/(1024*1024);
        long max = rt.maxMemory()/(1024*1024);

        MessageDialog.openInformation(
            null,
            "メモリ使用状況",
            String.format("Used :%dmb\nFree :%dmb\nTotal:%dmb\nMax  :%dmb",
                          total - free, free, total, max));


//        Utils.println(String.format("%d / %d / %d", (total - free), total, max));
    }
}


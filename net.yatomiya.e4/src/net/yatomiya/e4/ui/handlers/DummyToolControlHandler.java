/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.handlers;

import javax.annotation.*;
import org.eclipse.e4.core.di.annotations.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

/**
 * Top trim の最初の ToolControl として指定する。
 * トップトリムのツールバーを動かしてトップトリムのツールアイテムがなくなってしまうと自動的に
 * トップトリム領域が消えてしまうのだが、消えた後に動かそうとしてもトップトリムに移動させることができない。
 * 左右下のトリムは、消えた後にツールアイテムを移動させると復活するので、多分バグ。
 * workaround として、動かせないサイズ０のダミーコントロールをひとつ置いておいて、トップトリムのツールアイテムが
 * 必ず残るようにする。
 */
public class DummyToolControlHandler {
    @PostConstruct
    public void postConstruct(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
    }

    @Execute
    public void execute() {
    }
}

/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.yatomiya.e4.ui.workbench.renderers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

// net_yatomiya_e4_ui_workbench_renderers_swt
// org.eclipse.e4.ui.workbench.addons.minmax.TrimStack から org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout
// が直接参照されている。 net.yatomiya.e4.ui.workbench.renderers.swt.TrimmedPartLayout では別オブジェクトとして
// 判定されてしまうので、 org.eclipse.e4.ui.workbench.renderers.ui から引っ張ってくる。
// 基本的には org.eclipse.ui.workbench.renderers.swt を直接参照する必要はない設計になっているはずだが、
// 直接参照されている場合は同様の処置をいれる必要がある。
public class TrimmedPartLayout extends org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout {
    public TrimmedPartLayout(Composite parent) {
        super(parent);
    }
}

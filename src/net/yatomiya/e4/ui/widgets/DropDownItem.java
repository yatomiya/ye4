/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.util.*;

public class DropDownItem extends Composite {
    protected ToolBar bar;
    protected ToolItem item;

    public DropDownItem(Composite parent) {
        super(parent, SWT.NONE);

        setLayout(new FillLayout());

        bar = new ToolBar(this, SWT.HORIZONTAL);

        item = new ToolItem(bar, SWT.DROP_DOWN);
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if ((e.detail & SWT.ARROW) != 0)
                        doDropDown();
                    else
                        itemSelected();
                }
            });
    }

    public ToolItem getItem() {
        return item;
    }

    @Override
    public void setEnabled(boolean v) {
        super.setEnabled(v);
        bar.setEnabled(v);
    }

    protected void itemSelected() {
    }

    protected void doDropDown() {
        Menu menu = new Menu(getShell(), SWT.POP_UP);
        createDropDownMenu(menu);
        if (menu.getItemCount() == 0)
            return;

        Point loc = UIUtils.calcPopupMenuLocation(this);
        menu.setLocation(loc);
        menu.setVisible(true);
    }

    protected void createDropDownMenu(Menu parent) {
    }
}


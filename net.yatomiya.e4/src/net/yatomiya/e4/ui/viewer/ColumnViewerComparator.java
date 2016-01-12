/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;


public class ColumnViewerComparator extends ViewerComparator {
    private ColumnViewer viewer;
    private ColumnData comparingColumn;

    private class ColumnData {
        String type;
        boolean direction;
        Comparator comparator;
    }

    public ColumnViewerComparator(ColumnViewer viewer) {
        super();

        this.viewer = viewer;
        comparingColumn = null;
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        if (comparingColumn == null)
            return 0;

        int result = comparingColumn.comparator.compare(o1, o2);
        if (!comparingColumn.direction)
            result = -result;
        return result;
    }

    public static final String WIDGET_DATA = "ColumnViewerComparator.data";

    public void adapt(TableColumn col, String type, Comparator itemComparator) {
        ColumnData data = new ColumnData();
        data.type = type;
        data.direction = true;
        data.comparator = itemComparator;
        col.setData(WIDGET_DATA, data);

        col.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableColumn c = (TableColumn)e.widget;

                ColumnData data = (ColumnData)c.getData(WIDGET_DATA);
                data.direction = !data.direction;
                comparingColumn = data;

                c.getParent().setSortDirection(data.direction ? SWT.DOWN : SWT.UP);
                c.getParent().setSortColumn(c);

                viewer.refresh();
            }
            });
    }
}

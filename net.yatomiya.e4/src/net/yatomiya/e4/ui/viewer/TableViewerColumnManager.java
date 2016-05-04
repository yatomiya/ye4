/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.util.*;

public class TableViewerColumnManager {
    public static abstract class EditorAccessor {
        protected abstract Object getValue(Object element);
        protected abstract void setValue(Object element, Object value);
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    class InternalEditingSupport extends EditingSupport {
        ColumnData columnData;
        EditorAccessor accessor;
        CellEditor editor;

        InternalEditingSupport(ColumnViewer viewer, ColumnData columnData, EditorAccessor accessor, CellEditor editor) {
            super(viewer);

            this.columnData = columnData;
            this.accessor = accessor;
            this.editor = editor;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return accessor.getValue(element);
        }

        @Override
        protected void setValue(Object element, Object value) {
            for (IPropertyChangeListener l : propertyListenerList) {
                l.propertyChange(new PropertyChangeEvent(element, columnData.type, accessor.getValue(element), value));
            }

            for (Object e : SelectionUtils.toList(viewer.getSelection())) {
                accessor.setValue(e, value);
                viewer.update(e, null);
            }
        }
    }

    class InternalEditStrategy extends ColumnViewerEditorActivationStrategy {
        InternalEditStrategy(ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected boolean isEditorActivationEvent(
            ColumnViewerEditorActivationEvent event) {
            //元ソースは viewer.getSelection() の数が1のときのみエディターが起動される判定があるので、それを外す。
            boolean isLeftMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
                && ((MouseEvent)event.sourceEvent).button == 1;

            return (isLeftMouseSelect
                    || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
                    || event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
        }
    }

    TableViewer viewer;
    List<IPropertyChangeListener> propertyListenerList;

    class ColumnData {
        int index;
        String type;
        TableViewerColumn viewerColumn;
        InternalEditingSupport editingSupport;
        TableColumn tableColumn;
    }
    List<ColumnData> dataList;

    ISelection lastSelection;

    public TableViewerColumnManager(TableViewer viewer) {
        this.viewer = viewer;

        propertyListenerList = new ArrayList<>();

        dataList = new ArrayList<>();

        TableViewerEditor.create(
            viewer,
            new InternalEditStrategy(viewer),
            ColumnViewerEditor.DEFAULT);

        final Display display = viewer.getTable().getDisplay();
        final Listener mouseListener = new Listener() {
                @Override
                public void handleEvent(Event event) {
                    if (event.widget == viewer.getTable()) {
                        ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
                        if (cell != null) {
                            ColumnData data = dataList.get(cell.getColumnIndex());

                            boolean doRestoreSelection = false;
                            if (lastSelection != null
                                && SelectionUtils.size(lastSelection) > 1
                                && data.editingSupport != null) {
                                for (Object e : SelectionUtils.toList(lastSelection)) {
                                    if (e == cell.getElement())
                                        doRestoreSelection = true;
                                }
                            }

                            if (doRestoreSelection) {
                                viewer.setSelection(lastSelection);
                            } else {
                                lastSelection = viewer.getSelection();
                            }
                        }
                    }
                }
            };
        display.addFilter(SWT.MouseDown, mouseListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent event) {
                    display.removeFilter(SWT.MouseDown, mouseListener);
                }
            });
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        if (!propertyListenerList.contains(listener))
            propertyListenerList.add(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyListenerList.remove(listener);
    }

    public TableViewerColumn createColumn(
        String type, int style, String header, String tooltip, int width,
        CellLabelProvider labelProvider,
        Comparator<?> itemComparator,
        EditorAccessor accessor,
        CellEditor editor) {
        if (getColumnData(type) != null)
            throw new IllegalArgumentException();

        ColumnData data = new ColumnData();
        data.index = dataList.size();
        dataList.add(data);
        data.type = type;

        TableViewerColumn c = new TableViewerColumn(viewer, style);
        data.viewerColumn = c;

        c.setLabelProvider(labelProvider);
        if (accessor != null && editor != null) {
            InternalEditingSupport s = new InternalEditingSupport(viewer, data, accessor, editor);
            c.setEditingSupport(s);

            data.editingSupport = s;
        }

        if (viewer.getComparator() instanceof ColumnViewerComparator) {
            ColumnViewerComparator vcompartor = (ColumnViewerComparator)viewer.getComparator();
            vcompartor.adapt(c.getColumn(), type, itemComparator);
        }

        TableColumn col = c.getColumn();
        col.setText(header);
        col.setToolTipText(tooltip == null ? header : tooltip);
        col.setWidth(width);
        col.setResizable(true);
        col.setMoveable(true);
        data.tableColumn = col;

        return c;
    }

    public TableViewerColumn createColumn(
        String type, int style, String header, String tooltip, int width,
        CellLabelProvider labelProvider,
        Comparator<?> itemComparator) {
        return createColumn(type, style, header, tooltip, width, labelProvider, itemComparator, null, null);
    }

    ColumnData getColumnData(String type) {
        for (ColumnData d : dataList) {
            if (d.type.equals(type))
                return d;
        }
        return null;
    }

    ColumnData getColumnData(TableColumn c) {
        for (ColumnData d : dataList) {
            if (d.tableColumn == c)
                return d;
        }
        return null;
    }

    public String serialize() {
        Table table = viewer.getTable();

        TableColumn[] columns = table.getColumns();
        int[] columnOrder = table.getColumnOrder();
        String[] typeOrder = new String[columns.length];
        int[] width = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            TableColumn c = columns[columnOrder[i]];
            typeOrder[i] = getColumnData(c).type;
            width[i] = c.getWidth();
        }

        String s = IOUtils.serializeObject(new Object[] { typeOrder, width });
        return s;
    }

    public void deserialize(String serializedValue) {
        Object[] v = null;
        try {
            v = (Object[])IOUtils.deserializeObject(serializedValue);
        } catch (IOException e) {
            return;
        }

        String[] typeOrder = (String[])v[0];
        int[] width = (int[])v[1];

        Table table = viewer.getTable();
        TableColumn[] columns = table.getColumns();

        if (typeOrder.length != columns.length)
            return;
        for (int i = 0; i < typeOrder.length; i++) {
            if (getColumnData(typeOrder[i]) == null)
                return;
        }

        int[] columnOrder = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            ColumnData data = getColumnData(typeOrder[i]);
            TableColumn target = data.viewerColumn.getColumn();
            target.setWidth(width[i]);

            int targetIndex = -1;
            for (int j = 0; j < columns.length; j++) {
                if (target == columns[j]) {
                    targetIndex = j;
                    break;
                }
            }
            columnOrder[i] = targetIndex;
        }

        table.setColumnOrder(columnOrder);
    }

    public TableViewerColumn createTextColumn(
        String type, int style, String header, String tooltip, int width,
        ColumnLabelProvider labelProvider) {
        final ColumnLabelProvider lp = labelProvider;
        return createColumn(type, style, header, tooltip, width, labelProvider,
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return lp.getText(o1).compareTo(lp.getText(o2));
                                }
                            });
    }

    public TableViewerColumn createIntegerColumn(
        String type, int style, String header, String tooltip, int width, Function<Object, Integer> getter) {
        return createColumn(type, style, header, tooltip, width,
                            new ColumnLabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    return String.format("%d", JUtils.nonNull(getter.apply(element)));
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return Integer.compare(JUtils.nonNull(getter.apply(o1)), JUtils.nonNull(getter.apply(o2)));
                                }
                            });
    }

    public TableViewerColumn createBooleanColumn(
        String type, int style, String header, String tooltip, int width, Function<Object, Boolean> getter) {
        return createColumn(type, style, header, tooltip, width,
                            new ColumnLabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    return JUtils.nonNull(getter.apply(element)) ? "+" : "-";
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return Boolean.compare(JUtils.nonNull(getter.apply(o1)), JUtils.nonNull(getter.apply(o2)));
                                }
                            }
            );
    }

    public TableViewerColumn createDateColumn(
        String type, int style, String header, String tooltip, int width, Function<Object, Date> getter) {
        return createColumn(type, style, header, tooltip, width,
                            new ColumnLabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    Date date = getter.apply(element);
                                    return date == null ? "" : StringUtils.formatDate(date);
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    Date d1 = getter.apply(o1);
                                    Long l1 = d1 == null ? 0 : d1.getTime();
                                    Date d2 = getter.apply(o2);
                                    Long l2 = d2 == null ? 0 : d2.getTime();
                                    return Long.compare(l1, l2);
                                }
                            });
    }

    public TableViewerColumn createCheckBoxColumn(
        String type, int style, String header, String tooltip, int width, Function<Object, Boolean> getter) {
        return createColumn(type, style, header, tooltip, width,
                            new CheckBoxLabelProvider(viewer) {
                                @Override
                                protected boolean isChecked(Object element) {
                                    return JUtils.nonNull(getter.apply(element));
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return Boolean.compare(JUtils.nonNull(getter.apply(o1)), JUtils.nonNull(getter.apply(o2)));
                                }
                            }
            );
    }

    public TableViewerColumn createTextEditorColumn(
        String type, int style, String header, String tooltip, int width,
        EditorAccessor accessor) {
        return createColumn(type, style, header, tooltip, width,
                            new ColumnLabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    return JUtils.nonNull((String)accessor.getValue(element));
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return (JUtils.nonNull((String)accessor.getValue(o1))).compareTo(JUtils.nonNull((String)accessor.getValue(o2)));
                                }
                            },
                            accessor,
                            new TextCellEditor(viewer.getTable())
            );
    }

    public TableViewerColumn createCheckBoxEditorColumn(
        String type, int style, String header, String tooltip, int width,
        EditorAccessor accessor) {
        return createColumn(type, style, header, tooltip, width,
                            new CheckBoxLabelProvider(viewer) {
                                @Override
                                protected boolean isChecked(Object element) {
                                    return JUtils.nonNull((Boolean)accessor.getValue(element));
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return Boolean.compare(JUtils.nonNull((Boolean)accessor.getValue(o1)), JUtils.nonNull((Boolean)accessor.getValue(o2)));
                                }
                            },
                            accessor,
                            new CheckboxCellEditor(viewer.getTable(), SWT.CHECK) {
                                @Override
                                protected int getDoubleClickTimeout() {
                                    return 0;
                                }
                            }
            );
    }

    public TableViewerColumn createEnumEditorColumn(
        String type, int style, String header, String tooltip, int width,
        EditorAccessor accessor, String[] labelValues) {
        final String[] lv = labelValues;

        ShortComboBoxCellEditor editor = new ShortComboBoxCellEditor(viewer.getTable(), lv, SWT.READ_ONLY);
        editor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);

        return createColumn(type, style, header, tooltip, width,
                            new ColumnLabelProvider() {
                                @Override
                                public String getText(Object element) {
                                    return lv[JUtils.nonNull((Integer)accessor.getValue(element))];
                                }
                            },
                            new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return JUtils.nonNull((Integer)accessor.getValue(o1)) - JUtils.nonNull((Integer)accessor.getValue(o2));
                                }
                            },
                            accessor,
                            editor
            );
    }
}



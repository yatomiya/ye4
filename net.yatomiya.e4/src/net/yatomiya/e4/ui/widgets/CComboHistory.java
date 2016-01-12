/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import java.io.*;
import java.util.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public class CComboHistory {
    CCombo combo;
    int historyMaxCount = -1;
    String persistenceKey = null;

    static class PersistentData implements Serializable {
        java.util.List<String> historyList = new ArrayList<>();
    }

    public CComboHistory(CCombo combo, int historyMaxCount) {
        this(combo, historyMaxCount, null);
    }

    public CComboHistory(CCombo combo, int historyMaxCount, String persistenceKey) {
        this.combo = combo;
        this.historyMaxCount = historyMaxCount;
        this.persistenceKey = persistenceKey;

        combo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // item selected
                    addTextToHistory();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // text entered.
                    addTextToHistory();
                }
            });

        combo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (persistenceKey != null)
                        loadPersistedHistory();
                }
            });
    }

    public void removeItemAll () {
        // Combo.removeAll() clears text field.
        while (combo.getItemCount() > 0) {
            combo.remove(0);
        }
    }

    public void addTextToHistory() {
        addHistory(combo.getText());
    }

    public void addHistory(String item) {
        if (JUtils.isEmpty(item))
            return;

        List<String> list = new ArrayList<>(Arrays.asList(combo.getItems()));
        int index = list.indexOf(item);
        if (index >= 0) {
            list.remove(index);
        }
        list.add(0, item);
        if (list.size() > historyMaxCount)
            list.remove(list.size() - 1);

        removeItemAll();
        combo.setItems(list.toArray(new String[list.size()]));

        if (persistenceKey != null)
            savePersistedHistory();
    }

    public int getHistoryMaxCount() {
        return historyMaxCount;
    }

    public void setHistoryMaxCount(int v) {
        this.historyMaxCount = v;
    }

    public void loadPersistedHistory() {
        if (persistenceKey != null) {
            PersistentData pData = EUtils.get(PersistenceService.class)
                .get(persistenceKey, () -> new PersistentData());

            String[] array = pData.historyList.toArray(new String[pData.historyList.size()]);
            String[] itemArray = combo.getItems();
            if (!Arrays.equals(array, itemArray))
                combo.setItems(array);
        }
    }

    public void savePersistedHistory() {
        if (persistenceKey != null) {
            PersistentData pData = EUtils.get(PersistenceService.class)
                .get(persistenceKey, () -> new PersistentData());

            pData.historyList.clear();
            pData.historyList.addAll(Arrays.asList(combo.getItems()));
        }
    }

    public static CComboHistory configure(CCombo combo, int historyMaxCount, String persistenceKey) {
        CComboHistory h = new CComboHistory(combo, historyMaxCount, persistenceKey);
        combo.setData(CComboHistory.class.getName(), h);
        return h;
    }

    public static CComboHistory getCComboHistory(CCombo combo) {
        return (CComboHistory)combo.getData(CComboHistory.class.getName());
    }
}


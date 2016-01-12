/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import java.util.*;
import java.util.List;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public class ComboHistory {
    Combo combo;
    int historyMaxCount = -1;
    String persistenceKey = null;

    private static class PersistentData {
        List<String> historyList = new ArrayList<>();
    }

    public ComboHistory(Combo combo, int historyMaxCount) {
        this(combo, historyMaxCount, null);
    }

    public ComboHistory(Combo combo, int historyMaxCount, String persistenceKey) {
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
            combo.setItem(0, "");
            combo.remove(0);
        }
    }

    public void setItems(String[] items) {
        // Combo.setItems() clears text field.
        for (String s : items) {
            combo.add(s);
        }
    }

    public void addTextToHistory() {
        addHistory(combo.getText());
    }

    public void addHistory(String item) {
        if (JUtils.isEmpty(item))
            return;

        String text = combo.getText();

        List<String> list = new ArrayList<>(Arrays.asList(combo.getItems()));
        int index = list.indexOf(item);
        if (index >= 0) {
            list.remove(index);
        }
        list.add(0, item);

        if (list.size() > historyMaxCount)
            list.remove(list.size() - 1);
        removeItemAll();
        setItems(list.toArray(new String[list.size()]));

        // Combo.remove() でリストのアイテムを削除したときに、 Text 部分に同じ文字列があると Textもクリアされてしまう
        // そのクリアされたテキストを復活。
        if (!combo.getText().equals(text))
            combo.setText(text);

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
        PersistentData pData = EUtils.get(PersistenceService.class)
            .get(persistenceKey, () -> new PersistentData());

        String[] array = pData.historyList.toArray(new String[pData.historyList.size()]);
        String[] itemArray = combo.getItems();
        if (!Arrays.equals(array, itemArray)) {
            removeItemAll();
            setItems(array);
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

    public static ComboHistory configure(Combo combo, int historyMaxCount, String persistenceKey) {
        ComboHistory h = new ComboHistory(combo, historyMaxCount, persistenceKey);
        combo.setData(ComboHistory.class.getName(), h);
        return h;
    }

    public static ComboHistory getComboHistory(Combo combo) {
        return (ComboHistory)combo.getData(ComboHistory.class.getName());
    }
}


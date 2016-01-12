/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services.part;

import java.util.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public abstract class PartHistory {
    public static class Entry {
        String id = "";
        String label = "";

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Entry) {
                return id.equals(((Entry)o).id);
            }
            return super.equals(o);
        }
    }

    static class PersistentData {
        int maxCount = 32;
        LinkedList<Entry> historyList = new LinkedList<>();
    }

    PersistentData pData;

    protected void initialize() {
        pData = EUtils.get(PersistenceService.class).get(getPersistenceKey(), new PersistentData());

        PartUtils.subscribePartActivate(this, part -> {
                if (isTargetPart(part)) {
                    updateHistory(part);
                }
            });
        PartUtils.subscribePartClosed(this, part -> {
                if (isTargetPart(part)) {
                    updateHistory(part);
                }
            });
    }

    public void dispose() {
        EUtils.get(EventService.class).unsubscribe(this);
    }

    public List<Entry> getHistory() {
        return Collections.unmodifiableList(pData.historyList);
    }

    protected List<Entry> getInternalHistory() {
        return pData.historyList;
    }

    public abstract boolean isTargetPart(MPart part);

    public abstract String toId(MPart part);

    public abstract String toLabel(MPart part, String oldLabel);

    protected Entry updateHistory(MPart part) {
        String id = Objects.requireNonNull(toId(part));
        Entry entry = null;
        for (Entry e : pData.historyList) {
            if (e.getId().equals(id)) {
                entry = e;
                break;
            }
        }
        if (entry == null) {
            entry = new Entry();
            entry.id = id;
        } else {
            pData.historyList.remove(entry);
        }
        entry.label = toLabel(part, entry.getLabel());

        pData.historyList.addFirst(entry);

        capHistory();

        return entry;
    }

    public int getMaxCount() {
        return pData.maxCount;
    }

    private void capHistory() {
        while (pData.historyList.size() > pData.maxCount) {
            pData.historyList.removeLast();
        }
    }

    public void setMaxCount(int v) {
        if (pData.maxCount != v) {
            pData.maxCount = v;

            capHistory();
        }
    }

    protected abstract String getPersistenceKey();

    public abstract MPart openPart(int index);
}


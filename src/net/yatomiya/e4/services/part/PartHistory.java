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
import org.eclipse.e4.ui.workbench.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.services.*;

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

    private static class PersistentData {
        int maxCount = 32;
        LinkedList<Entry> historyList = new LinkedList<>();
    }

    private PersistentData pData;

    protected void initialize() {
        pData = AppUtils.get(PersistenceService.class).get(getPersistenceKey(), new PersistentData());

        EventService.Handler handler = e -> {
            doHandleEvent((EventService.UIEventData)e.getData());
        };
        AppUtils.get(EventService.class).subscribe(this, UIEvents.UILifeCycle.ACTIVATE, handler);
        AppUtils.get(EventService.class).subscribe(this, UIEvents.UIElement.TOPIC_TOBERENDERED, handler);
    }

    public void dispose() {
        AppUtils.get(EventService.class).unsubscribe(this);
    }

    public List<Entry> getHistory() {
        return Collections.unmodifiableList(pData.historyList);
    }

    protected void doHandleEvent(EventService.UIEventData event) {
        String topic = event.topic;
        if (!(event.element instanceof MPart))
            return;
        MPart part = (MPart)event.element;
        if (!isTargetPart(part))
            return;

        if (topic.equals(UIEvents.UILifeCycle.ACTIVATE)) {
            // When part is activated.
            updateHistory(part);
        } else if (event.attr.equals(UIEvents.UIElement.TOBERENDERED)
                   && event.isSET()
                   && (Boolean)event.newValue == Boolean.FALSE) {
            // When part is closed.
            updateHistory(part);
        }
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

    public abstract MPart showPart(int index);
}


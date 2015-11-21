/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services;

import java.util.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.e4.core.contexts.*;
import org.osgi.service.prefs.*;
import net.yatomiya.e4.*;

/**
 * uses InstanceScope only.
 */
public class PreferenceService {
    class ListenerBundle {
        class Entry {
            Object tag;
            String node;
            String key;
            Listener listener;
        }

        class NodeListener implements IEclipsePreferences.IPreferenceChangeListener {
            Map<String, List<Entry>> keyMap = new HashMap<>();

            @Override
            public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent e4Event) {
                String key = e4Event.getKey();
                List<Entry> list = keyMap.get(key);
                if (list != null) {
                    Event event = new Event(e4Event);
                    for (Entry e : list) {
                        e.listener.preferenceChange(event);
                    }
                }
            }
        }

        // node -> key -> listeners
        Map<String, NodeListener> nodeMap = new HashMap<>();
        Map<Object, List<Entry>> tagMap = new HashMap<>();

        public void subscribe(Object tag, String node, String key, Listener listener) {
            Entry entry = new Entry();
            entry.tag = tag;
            entry.node = node;
            entry.key = key;
            entry.listener = listener;

            NodeListener nodeListener = nodeMap.get(node);
            if (nodeListener == null) {
                nodeListener = new NodeListener();
                nodeMap.put(node, nodeListener);

                getInstancePreferences(node).addPreferenceChangeListener(nodeListener);
            }
            List<Entry> entryList = nodeListener.keyMap.get(key);
            if (entryList == null) {
                entryList = new ArrayList<>();
                nodeListener.keyMap.put(key, entryList);
            }
            entryList.add(entry);

            {
                List<Entry> list = tagMap.get(tag);
                if (list == null) {
                    list = new ArrayList<>();
                    tagMap.put(tag, list);
                }
                list.add(entry);
            }
        }

        public void unsubscribe(Object tag) {
            for (Entry e : tagMap.get(tag)) {
                NodeListener nodeListener = nodeMap.get(e.node);
                List<Entry> list = nodeListener.keyMap.get(e.key);
                list.remove(e);
                if (list.size() == 0) {
                    nodeListener.keyMap.remove(e.key);
                    if (nodeListener.keyMap.size() == 0) {
                        nodeMap.remove(e.node);

                        getInstancePreferences(e.node).removePreferenceChangeListener(nodeListener);
                    }
                }
            }
            tagMap.remove(tag);
        }
    }

    public static interface Listener {
        public void preferenceChange(Event event);
    }

    public class Event {
        private IEclipsePreferences.PreferenceChangeEvent e4Event;

        Event(IEclipsePreferences.PreferenceChangeEvent e4Event) {
            this.e4Event = e4Event;
        }

        public String getKey() {
            return e4Event.getKey();
        }

        public String getNewValue() {
            return (String)e4Event.getNewValue();
        }

        public String getOldValue() {
            return (String)e4Event.getOldValue();
        }

        public String getValue() {
            return getString(e4Event.getNode().name(), getKey());
        }

        public String getDefaultValue() {
            return getDefaultString(e4Event.getNode().name(), getKey());
        }

        public String getNodeName() {
            return e4Event.getNode().name();
        }

        public Preferences getPreferences() {
            return e4Event.getNode();
        }

        public PreferenceService getPreferenceService() {
            return PreferenceService.this;
        }
    }


    private IEclipseContext context;
    private String bundleId;
    private IPreferencesService e4Service;
    private ListenerBundle listenerBundle;

    public void initialize(IEclipseContext context) {
        this.context = context;

        Application app = context.get(Application.class);

        e4Service = context.get(IPreferencesService.class);

        listenerBundle = new ListenerBundle();
    }

    public void shutdown() {
        flushPreferences();
    }

    public String getString(String node, String key) {
        return e4Service.getString(node, key, "", null);
    }

    public String getDefaultString(String node, String key) {
        return e4Service.getString(node, key, "", new IScopeContext[] { BundleDefaultsScope.INSTANCE });
    }

    public void flushPreferences() {
        try {
            e4Service.getRootNode().flush();
        } catch (BackingStoreException e) {
            throw new IllegalStateException();
        }
    }

    IEclipsePreferences getInstancePreferences(String node) {
        return InstanceScope.INSTANCE.getNode(node);
    }

    public void subscribe(Object tag, String node, String key, Listener listener) {
        listenerBundle.subscribe(tag, node, key, listener);

        // call for initialization
        IEclipsePreferences prefs = getInstancePreferences(node);
        Object value = prefs.get(key, null);
        listener.preferenceChange(
            new Event(new IEclipsePreferences.PreferenceChangeEvent(prefs, key, value, value)));
    }

    public void unsubscribe(Object tag) {
        listenerBundle.unsubscribe(tag);
    }
}


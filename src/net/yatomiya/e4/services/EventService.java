/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services;

import java.util.*;
import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.services.events.*;
import org.eclipse.e4.ui.workbench.*;
import org.osgi.service.event.*;
import net.yatomiya.e4.util.*;


public class EventService {
    public static interface Handler {
        void handleEvent(Event event);
    }

    public static class Event {
        private String topic;
        private Object data;

        Event(org.osgi.service.event.Event e4Event) {
            topic = e4Event.getTopic();

            data = e4Event.getProperty(IEventBroker.DATA);
            if (data == null) {
                if (e4Event.getProperty(UIEvents.EventTags.ELEMENT) != null) {
                    data = new UIEventData(e4Event);
                }
            }
        }

        public String getTopic() {
            return topic;
        }

        public Object getData() {
            return data;
        }

        public UIEventData getUIEventData() {
            if (data instanceof UIEventData)
                return (UIEventData)data;
            return null;
        }
    }

    public static class UIEventData {
        public org.osgi.service.event.Event event;
        public String topic;
        public Object data;
        public String type;
        public String attr;
        public Object element;
        public Object widget;
        public Object oldValue;
        public Object newValue;

        public UIEventData(org.osgi.service.event.Event event) {
            this.event = event;
            topic = event.getTopic();
            data = event.getProperty(IEventBroker.DATA);
            element = event.getProperty(UIEvents.EventTags.ELEMENT);
            type = (String)event.getProperty(UIEvents.EventTags.TYPE);
            attr = (String)event.getProperty(UIEvents.EventTags.ATTNAME);
            widget = event.getProperty(UIEvents.EventTags.WIDGET);
            oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);
            newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
        }

        public boolean isADD() {
            return UIEvents.isADD(event);
        }

        public boolean isCREATE() {
            return UIEvents.isCREATE(event);
        }

        public boolean isREMOVE() {
            return UIEvents.isREMOVE(event);
        }

        public boolean isSET() {
            return UIEvents.isSET(event);
        }
    }

    private static class E4EventHandler implements EventHandler {
        Handler handler;

        E4EventHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void handleEvent(org.osgi.service.event.Event e4Event) {
            handler.handleEvent(new Event(e4Event));
        }
    }

    private class HandlerBundle extends ListBundle<Object, E4EventHandler> {
        public void subscribe(Object key, String topic, Handler handler) {
            E4EventHandler e4Handler = new E4EventHandler(handler);

            boolean result = eventBroker.subscribe(topic, e4Handler);
            if (!result)
                return;

            add(key, e4Handler);
        }

        public void unsubscribe(Object key) {
            List<E4EventHandler> list = getElements(key);
            if (list != null) {
                for (E4EventHandler h : list) {
                    eventBroker.unsubscribe(h);
                }
            }
            remove(key);
        }
    }

    private IEclipseContext context;
    private IEventBroker eventBroker;
    private HandlerBundle handlerBundle;

    public void initialize(IEclipseContext context) {
        this.context = context;

        eventBroker = context.get(IEventBroker.class);
        handlerBundle = new HandlerBundle();
    }

    public void shutdown() {
        handlerBundle.clear();
    }

    public void post(String topic, Object event) {
        eventBroker.post(topic, event);
    }

    public void send(String topic, Object event) {
        eventBroker.send(topic, event);
    }

    public void subscribe(String topic, Handler handler) {
        subscribe(handler, topic, handler);
    }

    public void subscribe(Object tag, String topic, Handler handler) {
        handlerBundle.subscribe(tag, topic, handler);
    }

    public void run(String topic, final Handler handler) {
        subscribe(handler, topic,
                  new Handler() {
                      @Override
                      public void handleEvent(Event event) {
                          handler.handleEvent(event);
                          unsubscribe(handler);
                      }
                  });
    }

    public void unsubscribe(Object tag) {
        handlerBundle.unsubscribe(tag);
    }

    public void updateUIEnablementAll() {
        eventBroker.post(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
    }
}


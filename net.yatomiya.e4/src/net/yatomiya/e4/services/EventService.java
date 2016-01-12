/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services;

import java.util.*;
import java.util.function.*;
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
        private org.osgi.service.event.Event osgiEvent;
        private Object data;

        Event(org.osgi.service.event.Event osgiEvent) {
            this.osgiEvent = osgiEvent;
            topic = osgiEvent.getTopic();

            if (osgiEvent.getProperty(UIEvents.EventTags.ELEMENT) != null) {
                data = new UIEventData(osgiEvent);
            } else {
                data = osgiEvent.getProperty(IEventBroker.DATA);
                if (data instanceof WrappedData) {
                    data = ((WrappedData)data).data;
                }
            }
        }

        public String getTopic() {
            return osgiEvent.getTopic();
        }

        public org.osgi.service.event.Event getOsgiEvent() {
            return osgiEvent;
        }

        public Object getData() {
            return data;
        }

        public UIEventData getUIEventData() {
            return (UIEventData)data;
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

    private class E4EventHandler implements EventHandler {
        Handler handler;

        E4EventHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void handleEvent(org.osgi.service.event.Event osgiEvent) {
            handler.handleEvent(new Event(osgiEvent));
        }
    }

    /**
     * osgi.Event はデータを String をキーにした Map で管理している。
     * IEventBroker.send()/post() で Map 以外をデータとして指定すると、 IEventBroker.DATA をキーとする
     * プロパティとしてセットされる。
     * Map だと、その要素は osgi.Event のマップに追加される。この Map のキーは String のみ。
     * String 以外のキーの Map を使えるようにするため、データをラップし send()/post() の内部で　Map と判定されないようにする。
     */
    private class WrappedData {
        Object data;

        WrappedData(Object data) {
            this.data = data;
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
        eventBroker.post(topic, new WrappedData(event));
    }

    public void send(String topic, Object event) {
        eventBroker.send(topic, new WrappedData(event));
    }

    public void subscribe(Object tag, String topic, Handler handler) {
        handlerBundle.subscribe(tag, topic, handler);
    }

    public <T> void subscribe(Object tag, String topic, Class<T> dataClass, Consumer<T> handler) {
        subscribe(tag, topic,
                  event -> {
                      Object data = event.getData();
                      if (data != null && dataClass.isInstance(data)) {
                          handler.accept((T)data);
                      }
                  });
    }

    public void subscribe(String topic, final Handler handler, int runCount) {
        subscribe(handler, topic,
                  new Handler() {
                      int count = runCount;

                      @Override
                      public void handleEvent(Event event) {
                          handler.handleEvent(event);

                          count--;
                          if (count <= 0)
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


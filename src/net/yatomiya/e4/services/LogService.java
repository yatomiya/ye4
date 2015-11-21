/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.services;

import org.eclipse.e4.core.contexts.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.util.*;

public class LogService {
    public static enum Level {
        INFO,
        WARN,
        ERROR,
        DEBUG,
    }

    public static enum Category {
        GENERAL,
        NETWORK,
        IO,
    }

    public static class LogEvent {
        private Level level;
        private Category category;
        private Throwable throwable;
        private String message;

        public LogEvent(Level level, Category category, Throwable throwable, String message) {
            this.level = level;
            this.category = category;
            this.throwable = throwable;
            this.message = message;
        }

        public Level getLevel() {
            return level;
        }

        public Category getCategory() {
            return category;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public String getMessage() {
            return message;
        }
    }

    public static interface Listener {
        void logging(LogEvent event);
    }

    private IEclipseContext context;
    private Application application;
    private ImmutableBitSet enabledLevelSet;
    private ListBundle<Object, Listener> listenerBundle;
    private org.eclipse.e4.core.services.log.Logger e4Logger;

    public void initialize(IEclipseContext context) {
        this.context = context;
        this.application = context.get(Application.class);

        enabledLevelSet = new ImmutableBitSet();
        setEnabled(Level.INFO, true);
        setEnabled(Level.WARN, true);
        setEnabled(Level.ERROR, true);

        listenerBundle = new ListBundle<>();
        e4Logger = context.get(org.eclipse.e4.core.services.log.Logger.class);
    }

    public void shutdown() {
    }

    public boolean isEnabled(Level level) {
        return enabledLevelSet.get(level.ordinal());
    }

    public void setEnabled(Level level, boolean v) {
        enabledLevelSet = enabledLevelSet.set(level.ordinal(), v);
    }

    public void subscribe(Object tag, Listener listener) {
        listenerBundle.add(tag, listener);
    }

    public void unsubscribe(Object tag) {
        listenerBundle.remove(tag);
    }

    public void log(Level level, Category category, Throwable throwable, String message, String... args) {
        if (AppUtils.isUIThread()) {
            doLog(level, category, throwable, message, args);
        } else {
            AppUtils.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        doLog(level, category, throwable, message, args);
                    }
                });
        }
    }

    private void doLog(Level level, Category category, Throwable throwable, String message, String... args) {
        if (!isEnabled(level))
            return;

        String formattedMessage = String.format("[%s] [%s] %s",
                                                level.name(),
                                                category.name(),
                                                String.format(message, (Object)args));

        LogEvent event = new LogEvent(level, category, throwable, formattedMessage);
        for (Listener l : listenerBundle.getElementsAll()) {
            l.logging(event);
        }

        switch (level) {
        case INFO:
            break;
        case WARN:
            break;
        case ERROR:
            e4Logger.error(throwable, formattedMessage);
            break;
        case DEBUG:
            break;
        }

        if (application.isDevelopmentMode() && level != Level.INFO) {
            JUtils.println(formattedMessage);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }

    public void log(Level level, Category category, String message, String... args) {
        log(level, category, null, message, args);
    }

    private static void staticLog(Level level, Category category, Throwable throwable, String message, String... args) {
        AppUtils.get(LogService.class).log(level, category, throwable, message, args);
    }

    public static void info(Category category, String message, String... args) {
        staticLog(Level.INFO, category, null, message, args);
    }

    public static void info(String message, String... args) {
        info(Category.GENERAL, message, args);
    }

    public static void warn(Category category, Throwable throwable, String message, String... args) {
        staticLog(Level.WARN, category, throwable, message, args);
    }

    public static void error(Category category, Throwable throwable, String message, String... args) {
        staticLog(Level.ERROR, category, throwable, message, args);
    }

    public static void error(Throwable throwable) {
        error(Category.GENERAL, throwable, "");
    }

    public static void debug(Category category, String message, String... args) {
        staticLog(Level.DEBUG, category, null, message, args);
    }
}



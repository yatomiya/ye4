/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4;

import java.io.*;
import org.eclipse.core.runtime.*;
import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.core.services.events.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import org.eclipse.e4.ui.workbench.*;
import org.eclipse.swt.widgets.*;
import org.osgi.framework.*;
import org.osgi.service.event.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.services.image.*;
import net.yatomiya.e4.services.part.*;
import net.yatomiya.e4.util.*;

public abstract class Application {
    public static final String PLUGIN_ID = "net.yatomiya.e4";
    private static Application thisInstance;

    private IEclipseContext context;
    private Bundle productBundle;

    private File installPath;
    private File instancePath;
    private File configurationPath;
    private File userPath;
    private File tempPath;
    private File dataPath;
    private File cachePath;

    private boolean isFirstLaunch;
    private RepeatableRunner backgroundSaver;

    public void initialize(IEclipseContext context) {
        this.context = context;

        context.set(Application.class, this);
        thisInstance = this;

        IProduct product = Platform.getProduct();
        productBundle = product.getDefiningBundle();
        Version v = productBundle.getVersion();

        {
            installPath = new File(Platform.getInstallLocation().getURL().getPath());
            instancePath = new File(Platform.getInstanceLocation().getURL().getPath());
            configurationPath = new File(new File(Platform.getConfigurationLocation().getURL().getPath()), getProductPluginId());
            userPath = new File(Platform.getUserLocation().getURL().getPath());

            try {
                if (isDevelopmentMode()) {
                    dataPath = instancePath;
                } else {
                    dataPath = installPath;
                }
                dataPath = new File(dataPath, "data");

                isFirstLaunch = !IOUtils.isDirectoryExists(dataPath);

                IOUtils.createDirectory(dataPath);

                cachePath = new File(dataPath, "cache");
                IOUtils.createDirectory(cachePath);

                tempPath = new File(dataPath, "temp");
                IOUtils.createDirectory(tempPath);
            } catch (IOException e) {
                throw new IllegalStateException("Creating data directories failed.", e);
            }
        }

        context.get(IEventBroker.class).subscribe(
            UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
            new EventHandler() {
                @Override
                public void handleEvent(org.osgi.service.event.Event event) {
                    initializeUI();
                    context.get(IEventBroker.class).unsubscribe(this);
                }
            });

        backgroundSaver = new RepeatableRunner(
            (int)(Math.random() * (1000*60*5)), 1000*60*5,
            () -> {
                try {
                    context.get(IModelResourceHandler.class).save();
                } catch (IOException e) {
                }
            });
    }

    protected void createServices() {
        context.set(LogService.class, new LogService());
        context.set(EventService.class, new EventService());
        context.set(PersistenceService.class, new PersistenceService());
        context.set(PreferenceService.class, new PreferenceService());
        context.set(PartService.class, new PartService());
        context.set(ImageService.class, new ImageService());

        context.get(LogService.class).initialize(context);
        context.get(EventService.class).initialize(context);
        context.get(PersistenceService.class).initialize(context);
        context.get(PreferenceService.class).initialize(context);
        context.get(PartService.class).initialize(context);
        context.get(ImageService.class).initialize(context);
    }

    protected void shutdownServices() {
        context.get(ImageService.class).shutdown();
        context.get(PartService.class).shutdown();
        context.get(PreferenceService.class).shutdown();
        context.get(PersistenceService.class).shutdown();
        context.get(EventService.class).shutdown();
        context.get(LogService.class).shutdown();
    }

    public void initializeUI() {
        context.get(PartService.class).initializeUI();

        /**
         * workaround
         * ときどき MainMenu が消えてしまう不具合がある。
         * モデルは生成されているが、 Widget が生成されていない。モデルエディターで ToBeRenderered オンオフすると復活する。
         */
        new RepeatableRunner(1000, 1000) {
            @Override
            public void run() {
                if (context == null)
                    return;
                MWindow win = EModelUtils.getActiveWindow();
                if (win != null) {
                    MMenu menuModel = win.getMainMenu();
                    Menu menu = (Menu)menuModel.getWidget();
                    if (menu != null) {
                        if (menu.getItems().length == 0) {
                            menuModel.setToBeRendered(false);
                            menuModel.setToBeRendered(true);
                        }
                    }
                    cancel();
                }
            }
        }.start();
    }

    public void shutdown() {
        backgroundSaver.cancel();

        shutdownServices();

        try {
            IOUtils.deleteDirectory(tempPath);
        } catch (IOException e) {
        }

        context = null;
    }

    public IEclipseContext getContext() {
        return context;
    }

    public <T> T get(Class<T> clazz) {
        return context.get(clazz);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public String getProductPluginId() {
        return productBundle.getSymbolicName();
    }

    public Bundle getProductBundle() {
        return productBundle;
    }

    public boolean isDevelopmentMode() {
        return Platform.inDevelopmentMode();
    }

    public File getInstallPath() {
        return installPath;
    }

    public File getInstancePath() {
        return instancePath;
    }

    public File getConfigurationPath() {
        return configurationPath;
    }

    public File getUserPath() {
        return userPath;
    }

    public File getDataPath() {
        return dataPath;
    }

    public File getCachePath() {
        return cachePath;
    }

    public File getTempPath() {
        return tempPath;
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public static Application getInstance() {
        return thisInstance;
    }
}

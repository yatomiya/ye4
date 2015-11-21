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
import org.eclipse.e4.ui.workbench.*;
import org.osgi.framework.*;
import net.yatomiya.e4.services.*;
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

    public void initialize(IEclipseContext context) {
        this.context = context;

        context.set(Application.class, this);
        thisInstance = this;

        IProduct product = Platform.getProduct();
        productBundle = product.getDefiningBundle();

        {
            boolean result = true;

            installPath = new File(Platform.getInstallLocation().getURL().getPath());
            instancePath = new File(Platform.getInstanceLocation().getURL().getPath());
            configurationPath = new File(new File(Platform.getConfigurationLocation().getURL().getPath()), getProductPluginId());
            userPath = new File(Platform.getUserLocation().getURL().getPath());

            {
                if (isDevelopmentMode()) {
                    dataPath = instancePath;
                } else {
                    dataPath = installPath;
                }
                dataPath = new File(dataPath, "data");

                isFirstLaunch = !IOUtils.isDirectoryExists(dataPath);

                result &= IOUtils.checkAndCreateDirectory(dataPath);
            }


            {
                cachePath = new File(dataPath, "cache");
                result &= IOUtils.checkAndCreateDirectory(cachePath);
            }

            {
                tempPath = new File(dataPath, "temp");
                result &= IOUtils.forceDeleteDirectory(tempPath);
                result &= IOUtils.checkAndCreateDirectory(tempPath);
            }

            if (!result)
                throw new RuntimeException("Creating data directories failed.");
        }

        createServices();

        context.get(EventService.class).subscribe(
            this, UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
            e -> {
                initializeUI();
                context.get(EventService.class).unsubscribe(this);
            });
    }

    public void initializeUI() {
        context.get(PartService.class).initializeUI();
    }

    public void shutdown() {
        shutdownServices();

        IOUtils.forceDeleteDirectory(tempPath);

        context = null;
    }

    protected void createServices() {
    }

    protected void shutdownServices() {
    }

    public IEclipseContext getContext() {
        return context;
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

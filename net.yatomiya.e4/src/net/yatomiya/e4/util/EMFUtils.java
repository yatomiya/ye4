/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.*;
import org.eclipse.emf.ecore.util.*;
import org.eclipse.emf.ecore.xmi.*;
import org.eclipse.emf.ecore.xmi.impl.*;


public class EMFUtils {
    public static <T extends EObject> T copy(T src) {
        return EcoreUtil.copy(src);
    }

    public static boolean equals(EObject o1, EObject o2) {
        return EcoreUtil.equals(o1, o2);
    }

    public static <T> T createInstance(EPackage pkg, Class<T> clazz) {
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c.getInstanceClass().equals(clazz)) {
                return (T)pkg.getEFactoryInstance().create((EClass)c);
            }
        }

        return null;
    }

    public static EObject load(File path) throws IOException {
        InputStream ins = new BufferedInputStream(new FileInputStream(path));
        EObject obj = load(ins);
        ins.close();
        return obj;
    }

    public static EObject load(InputStream in) throws IOException {
        Resource rs = new XMIResourceImpl();
        rs.load(in, createResourceOptions());
        return rs.getContents().get(0);
    }

    public static void save(File path, EObject mObj) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
        save(out, mObj);
        out.close();
    }

    public static void save(OutputStream out, EObject mObj) throws IOException {
        Resource rs = new XMIResourceImpl();
        rs.getContents().add(mObj);
        rs.save(out, createResourceOptions());
    }

    private static Map createResourceOptions() {
        HashMap options = new HashMap();
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
        options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
        options.put(XMLResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
        options.put(XMIResource.OPTION_SUPPRESS_XMI, Boolean.TRUE);
        return options;
    }

}

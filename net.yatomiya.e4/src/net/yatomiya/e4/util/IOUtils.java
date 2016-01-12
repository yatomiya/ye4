/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
import com.thoughtworks.xstream.*;
import net.yatomiya.e4.*;

public class IOUtils {
    public static boolean isFileExists(File file) {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isDirectoryExists(File dir) {
        return dir != null && dir.exists() && dir.isDirectory();
    }

    public static void createDirectory(File dir) throws IOException {
        if (isDirectoryExists(dir))
            return;

        boolean result = dir.mkdirs();
        if (!result)
            throw new IOException(dir.toString() + " mkdir() failed.");
    }

    public static void delete(File file) throws IOException {
        Files.delete(file.toPath());
    }

    public static void deleteDirectory(File dir) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                if (child.isDirectory()) {
                    deleteDirectory(child);
                } else {
                    Files.delete(child.toPath());
                }
            }
            Files.delete(dir.toPath());
        }
    }

    public static InputStream getBundleInputStream(Bundle bundle, String path) throws IOException {
        URL url = FileLocator.find(bundle, new org.eclipse.core.runtime.Path(path), null);
        return url.openConnection().getInputStream();
    }

    public static byte[] read(InputStream ins) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024*10];
        int length = 0;
        while ((length = ins.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        ins.close();
        return out.toByteArray();
    }

    public static byte[] read(File file) throws IOException {
        return read(new BufferedInputStream(new FileInputStream(file)));
    }

    public static byte[] read(URL url) throws IOException {
        return read(url.openConnection().getInputStream());
    }

    public static String readString(InputStream ins, String charset) throws IOException {
        return new String(read(ins), charset);
    }

    public static String readString(InputStream ins) throws IOException {
        return readString(ins, "UTF-8");
    }

    public static String readString(URL url) throws IOException {
        return readString(url.openConnection().getInputStream());
    }

    public static String readString(File file) throws IOException {
        return readString(file, "UTF-8");
    }

    public static String readString(File file, String charset) throws IOException {
        return readString(new BufferedInputStream(new FileInputStream(file)), charset);
    }

    public static void write(File file, InputStream ins) throws IOException {
        write(file, read(ins));
    }

    public static void write(File file, byte[] buf) throws IOException {
        write(file, buf, false);
    }

    public static void write(File file, byte[] buf, boolean append) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, append));
        out.write(buf);
        out.close();
    }

    public static void write(File file, String text, String charset) throws IOException {
        write(file, text.getBytes(charset));
    }

    public static void write(File file, String text) throws IOException {
        write(file, text, "UTF-8");
    }

    public static void copy(File src, File dst) throws IOException {
        Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static InputStream ungzip(InputStream ins) throws IOException {
        return new GZIPInputStream(ins);
    }

    public static OutputStream gzip(OutputStream out) throws IOException {
        return new GZIPOutputStream(out);
    }

    public static Object readObject(InputStream ins) throws IOException {
        return readObject(ins, false);
    }

    public static Object readObject(InputStream ins, boolean gzip) throws IOException {
        if (gzip)
            ins = ungzip(ins);
        Object obj = deserializeObject(readString(ins));
        ins.close();
        return obj;
    }

    public static Object readObject(File file) throws IOException {
        return readObject(file, false);
    }

    public static Object readObject(File file, boolean gzip) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        Object obj = readObject(in, gzip);
        in.close();
        return obj;
    }

    public static void writeObject(OutputStream out, Object obj) throws IOException {
        String xml = serializeObject(obj);
        out.write(xml.getBytes("UTF-8"));
        out.close();
    }

    public static void writeObject(File file, Object obj) throws IOException {
        writeObject(file, obj, false);
    }

    public static void writeObject(File file, Object obj, boolean gzip) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        if (gzip)
            out = gzip(out);
        writeObject(out, obj);
        out.close();
    }

    static XStream xstream;

    private static void initializeXStream() {
        if (xstream != null)
            return;

        // Plugin has each own ClassLoader. If not configured, XStream uses its own ClassLoader.
        // That default ClassLoader can access only classes which can be accessed from plugin in which XStream jar is included.
        // We need ClassLoader which can access all classes used in application, that is,
        // CalssLoader of application plugin. We retrieve the ClassLoader from implementation
        // of Application, which is supposed to be declared in application plugin.


        ClassLoader cl = Application.getInstance().getClass().getClassLoader();

        xstream = new XStream();
        xstream.setClassLoader(cl);
        xstream.ignoreUnknownElements();
    }


    public static String serializeObject(Object obj) {
        initializeXStream();

        return xstream.toXML(obj);
    }

    public static Object deserializeObject(String str) throws IOException {
        try {
            initializeXStream();

            return xstream.fromXML(str);
        } catch (XStreamException e) {
            throw new IOException(e);
        }
    }
}



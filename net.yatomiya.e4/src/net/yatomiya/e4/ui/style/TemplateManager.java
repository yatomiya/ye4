/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import java.io.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import groovy.lang.*;
import net.yatomiya.e4.util.*;

public class TemplateManager {
    static final String TAG_TEMPLATE_NAME = "template";
    static final String TAG_ATTRIBUTE_NAME = "attribute";
    static final String TAG_SCRIPT_NAME = "script";
    static final String TAG_LOAD_SCRIPT_NAME = "load_script";

    private String templateString;
    private Element templateBody;
    private Map<String, Element> templateMap;
    private Map<String, Element> attributeMap;

    private GroovyShell scriptShell;
    private Map<String, Script> scriptCacheMap;


    public TemplateManager(String templateString, File baseDir) throws IOException {
        templateBody = Jsoup.parse(templateString).body();

        templateMap = new HashMap<>();
        attributeMap = new HashMap<>();
        scriptShell = new GroovyShell();
        for (Node node : templateBody.childNodes()) {
            if (node instanceof Element) {
                Element e = (Element)node;
                if (e.tagName().equals(TAG_TEMPLATE_NAME)) {
                    String id = e.attr("id");
                    if (!JUtils.isEmpty(id)) {
                        templateMap.put(id, e);
                    }
                } else if (e.tagName().equals(TAG_ATTRIBUTE_NAME)) {
                    String tag = e.attr("tag");
                    if (!JUtils.isEmpty(tag)) {
                        attributeMap.put(tag, e);
                    }
                } else if (e.tagName().equals(TAG_SCRIPT_NAME)) {
                    String s = e.data();
                    scriptShell.evaluate(s);
                } else if (e.tagName().equals(TAG_LOAD_SCRIPT_NAME)) {
                    String filename = e.attr("file");
                    if (!JUtils.isEmpty(filename)) {
                        String s = IOUtils.readString(new File(baseDir, filename));
                        scriptShell.evaluate(s);
                    }
                }
            }
        }

        scriptCacheMap = new HashMap<>();
    }

    public Element getTemplate(String id) {
        return templateMap.get(id);
    }

    public Attributes getAttributeTemplate(String tag) {
        Element e = attributeMap.get(tag);
        if (e != null) {
            return e.attributes();
        }
        return null;
    }

    public Script getScript(String scriptString) {
        Script script = scriptCacheMap.get(scriptString);
        if (script == null) {
            script = scriptShell.parse(scriptString);
            scriptCacheMap.put(scriptString, script);
        }
        return script;
    }
}


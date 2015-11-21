/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import net.yatomiya.e4.util.*;

public class TemplateManager {
    static final String TEMPLATE_DIR = "template";
    static final String TEMPLATE_FILE = "style.template";
    static final String TAG_TEMPLATE_NAME = "define_template";
    static final String TAG_STYLE_NAME = "define_style";

    private String templateString;
    private Element templateBody;
    private Map<String, Element> templateMap;
    private Map<String, Element> styleMap;

    public TemplateManager(String templateString) {
        templateBody = Jsoup.parse(templateString).body();

        templateMap = new HashMap<>();
        styleMap = new HashMap<>();
        for (Node node : templateBody.childNodes()) {
            if (node instanceof Element) {
                Element e = (Element)node;
                String id = e.attr("id");
                if (JUtils.isNotEmpty(id)) {
                    if (e.tagName().equals(TAG_TEMPLATE_NAME)) {
                        templateMap.put(id, e);
                    }
                    if (e.tagName().equals(TAG_STYLE_NAME)) {
                        styleMap.put(id, e);
                    }
                }
            }
        }
    }

    public Element getTemplate(String id) {
        return templateMap.get(id);
    }

    public Attributes getStyleTemplate(String id) {
        Element e = styleMap.get(id);
        if (e != null) {
            return e.attributes();
        }
        return null;
    }
}


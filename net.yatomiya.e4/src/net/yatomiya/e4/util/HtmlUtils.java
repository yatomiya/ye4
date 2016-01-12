/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;



public class HtmlUtils {
    public static void removeTag(Document doc, String selector) {
        for (Element e : doc.select(selector).toArray(new Element[0])) {
            String text = e.text();
            e.after(text);
            e.remove();
        }
    }

    public static void removeElement(Document doc, String selector) {
        for (Element e : doc.select(selector).toArray(new Element[0])) {
            e.remove();
        }
    }

    public static String toReadableText(String html) {
        return toReadableText(Jsoup.parse(html));
    }

    public static String toReadableText(Document doc) {
        final String BR_REPLACE_CODE = HtmlUtils.class.getName() + "_BR_REPLACE_CODE";
        for (Element e : new ArrayList<>(doc.select("br"))) {
            e.after(BR_REPLACE_CODE);
            e.remove();
        }
        String text = doc.text();
        text = text.replace(BR_REPLACE_CODE, "\n");

        return text;
    }

    public static String unescapeHtml(String str) {
        return Parser.unescapeEntities(str, false);
    }

    // "#rrggbb" or "rrggbb"
    public static int[] hexToRgb(String colorStr) {
        String s = colorStr;
        if (s.charAt(0) == '#')
            s = s.substring(1, s.length());
        int[] result = new int[3];
        try {
            result[0] = Integer.valueOf(s.substring(0, 2), 16);
            result[1] = Integer.valueOf(s.substring(2, 4), 16);
            result[2] = Integer.valueOf(s.substring(4, 6), 16);
        } catch (NumberFormatException e) {
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
        }
        return result;
    }

    public static String rgbToHex(int[] rgb) {
        return String.format("%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
    }

    public static Map<String, String> retrieveHiddenInputs(Document doc) {
        Map<String, String> map = new HashMap<>();
        for (Element e : doc.select("form input[type=hidden]")) {
            String name = e.attr("name");
            String value = e.attr("value");
            map.put(name, value);
        }
        return map;
    }

}


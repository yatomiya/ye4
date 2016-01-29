/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.util.*;
import java.util.regex.*;
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

    public static final String HEX_COLOR_EXPR = "^#[A-Fa-f0-9]{6}$";
    public static final Pattern HEX_COLOR_PATTERN = Pattern.compile(HEX_COLOR_EXPR);
    public static final String RGB_COLOR_EXPR = "^[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}$";
    public static final Pattern RGB_COLOR_PATTERN = Pattern.compile(RGB_COLOR_EXPR);

    public static int[] parseColor(String colorString) {
        int[] rgb = null;
        if (HEX_COLOR_PATTERN.matcher(colorString).find())
            rgb = parseHexColor(colorString);
        else if (RGB_COLOR_PATTERN.matcher(colorString).find())
            rgb = parseRgbColor(colorString);
        else
            rgb = parseX11ColorName(colorString);
        if (rgb == null)
            throw new IllegalArgumentException();
        return rgb;
    }

    // "#rrggbb"
    public static int[] parseHexColor(String colorString) {
        String s = colorString;
        s = s.substring(1, s.length());
        int[] rgb = new int[3];
        rgb[0] = Integer.valueOf(s.substring(0, 2), 16);
        rgb[1] = Integer.valueOf(s.substring(2, 4), 16);
        rgb[2] = Integer.valueOf(s.substring(4, 6), 16);
        return rgb;
    }

    public static String rgbToHex(int[] rgb) {
        return String.format("%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
    }

    // "12,8,200"
    public static int[] parseRgbColor(String colorString) {
        String[] n = StringUtils.split(colorString, ",");
        int[] rgb = new int[3];
        rgb[0] = Integer.valueOf(n[0]);
        rgb[1] = Integer.valueOf(n[1]);
        rgb[2] = Integer.valueOf(n[2]);
        return rgb;
    }

    private static final String[] X11_COLOR_NAMES = {
        "aliceblue", 	"#f0f8ff",
        "antiquewhite", 	"#faebd7",
        "aqua", 	"#00ffff",
        "aquamarine", 	"#7fffd4",
        "azure", 	"#f0ffff",
        "beige", 	"#f5f5dc",
        "bisque", 	"#ffe4c4",
        "black", 	"#000000",
        "blanchedalmond", 	"#ffebcd",
        "blue", 	"#0000ff",
        "blueviolet", 	"#8a2be2",
        "brown", 	"#a52a2a",
        "burlywood", 	"#deb887",
        "cadetblue", 	"#5f9ea0",
        "chartreuse", 	"#7fff00",
        "chocolate", 	"#d2691e",
        "coral", 	"#ff7f50",
        "cornflowerblue", 	"#6495ed",
        "cornsilk", 	"#fff8dc",
        "crimson", 	"#dc143c",
        "cyan", 	"#00ffff",
        "darkblue", 	"#00008b",
        "darkcyan", 	"#008b8b",
        "darkgoldenrod", 	"#b8860b",
        "darkgray", 	"#a9a9a9",
        "darkgreen", 	"#006400",
        "darkkhaki", 	"#bdb76b",
        "darkmagenta", 	"#8b008b",
        "darkolivegreen", 	"#556b2f",
        "darkorange", 	"#ff8c00",
        "darkorchid", 	"#9932cc",
        "darkred", 	"#8b0000",
        "darksalmon", 	"#e9967a",
        "darkseagreen", 	"#8fbc8f",
        "darkslateblue", 	"#483d8b",
        "darkslategray", 	"#2f4f4f",
        "darkturquoise", 	"#00ced1",
        "darkviolet", 	"#9400d3",
        "deeppink", 	"#ff1493",
        "deepskyblue", 	"#00bfff",
        "dimgray", 	"#696969",
        "dodgerblue", 	"#1e90ff",
        "firebrick", 	"#b22222",
        "floralwhite", 	"#fffaf0",
        "forestgreen", 	"#228b22",
        "fuchsia", 	"#ff00ff",
        "gainsboro", 	"#dcdcdc",
        "ghostwhite", 	"#f8f8ff",
        "gold", 	"#ffd700",
        "goldenrod", 	"#daa520",
        "gray", 	"#808080",
        "green", 	"#008000",
        "greenyellow", 	"#adff2f",
        "honeydew", 	"#f0fff0",
        "hotpink", 	"#ff69b4",
        "indianred", 	"#cd5c5c",
        "indigo", 	"#4b0082",
        "ivory", 	"#fffff0",
        "khaki", 	"#f0e68c",
        "lavender", 	"#e6e6fa",
        "lavenderblush", 	"#fff0f5",
        "lawngreen", 	"#7cfc00",
        "lemonchiffon", 	"#fffacd",
        "lightblue", 	"#add8e6",
        "lightcoral", 	"#f08080",
        "lightcyan", 	"#e0ffff",
        "lightgoldenrodyellow", 	"#fafad2",
        "lightgreen", 	"#90ee90",
        "lightgrey", 	"#d3d3d3",
        "lightpink", 	"#ffb6c1",
        "lightsalmon", 	"#ffa07a",
        "lightseagreen", 	"#20b2aa",
        "lightskyblue", 	"#87cefa",
        "lightslategray", 	"#778899",
        "lightsteelblue", 	"#b0c4de",
        "lightyellow", 	"#ffffe0",
        "lime", 	"#00ff00",
        "limegreen", 	"#32cd32",
        "linen", 	"#faf0e6",
        "magenta", 	"#ff00ff",
        "maroon", 	"#800000",
        "mediumaquamarine", 	"#66cdaa",
        "mediumblue", 	"#0000cd",
        "mediumorchid", 	"#ba55d3",
        "mediumpurple", 	"#9370db",
        "mediumseagreen", 	"#3cb371",
        "mediumslateblue", 	"#7b68ee",
        "mediumspringgreen", 	"#00fa9a",
        "mediumturquoise", 	"#48d1cc",
        "mediumvioletred", 	"#c71585",
        "midnightblue", 	"#191970",
        "mintcream", 	"#f5fffa",
        "mistyrose", 	"#ffe4e1",
        "moccasin", 	"#ffe4b5",
        "navajowhite", 	"#ffdead",
        "navy", 	"#000080",
        "oldlace", 	"#fdf5e6",
        "olive", 	"#808000",
        "olivedrab", 	"#6b8e23",
        "orange", 	"#ffa500",
        "orangered", 	"#ff4500",
        "orchid", 	"#da70d6",
        "palegoldenrod", 	"#eee8aa",
        "palegreen", 	"#98fb98",
        "paleturquoise", 	"#afeeee",
        "palevioletred", 	"#db7093",
        "papayawhip", 	"#ffefd5",
        "peachpuff", 	"#ffdab9",
        "peru", 	"#cd853f",
        "pink", 	"#ffc0cb",
        "plum", 	"#dda0dd",
        "powderblue", 	"#b0e0e6",
        "purple", 	"#800080",
        "red", 	"#ff0000",
        "rosybrown", 	"#bc8f8f",
        "royalblue", 	"#4169e1",
        "saddlebrown", 	"#8b4513",
        "salmon", 	"#fa8072",
        "sandybrown", 	"#f4a460",
        "seagreen", 	"#2e8b57",
        "seashell", 	"#fff5ee",
        "sienna", 	"#a0522d",
        "silver", 	"#c0c0c0",
        "skyblue", 	"#87ceeb",
        "slateblue", 	"#6a5acd",
        "slategray", 	"#708090",
        "snow", 	"#fffafa",
        "springgreen", 	"#00ff7f",
        "steelblue", 	"#4682b4",
        "tan", 	"#d2b48c",
        "teal", 	"#008080",
        "thistle", 	"#d8bfd8",
        "tomato", 	"#ff6347",
        "turquoise", 	"#40e0d0",
        "violet", 	"#ee82ee",
        "wheat", 	"#f5deb3",
        "white", 	"#ffffff",
        "whitesmoke", 	"#f5f5f5",
        "yellow", 	"#ffff00",
        "yellowgreen", 	"#9acd32",
    };

    private static Map<String, int[]> x11ColorMap;

    public static Map<String, int[]> getX11ColorMap() {
        if (x11ColorMap == null) {
            x11ColorMap = new HashMap<>();
            for (int i = 0; i < X11_COLOR_NAMES.length / 2; i++) {
                String name = X11_COLOR_NAMES[i*2];
                String cs = X11_COLOR_NAMES[i*2 + 1];
                int[] rgb = parseHexColor(cs);
                x11ColorMap.put(name, rgb);
            }
        }
        return Collections.unmodifiableMap(x11ColorMap);
    }

    public static int[] parseX11ColorName(String name) {
        name = name.toLowerCase();
        return getX11ColorMap().get(name);
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


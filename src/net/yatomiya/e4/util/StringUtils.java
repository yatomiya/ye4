/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class StringUtils {
    public static final char UNICODE_OBJECT_REPLACEMENT_0 = '\ufffc';
    public static final char UNICODE_OBJECT_REPLACEMENT_1 = '\ufffd';

    public static interface MatchResultProcessor {
        String process(Matcher matcher);
    }

    public static final Pattern STRING_END_NEWLINE_PATTERN = Pattern.compile("[\\r\\n]+$");
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static int countChar(CharSequence str, char c) {
        int count =0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static int countLine(CharSequence str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n')
                count++;
        }
        if ((str.length() > 0) && (str.charAt(str.length() - 1) != '\n'))
            count++;
        return count;
    }

    public static byte[] getBytes(String v, String charset) {
        try {
            return v.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String getString(byte[] buf, String charset) {
        try {
            return new String(buf, charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String capitalize(String str) {
        if (str.length() == 0)
            return str;

        String s = str.substring(0, 1).toUpperCase();
        if (str.length() > 2)
            s += str.substring(1);
        return s;
    }

    public static String ensureEndIsNewLine(String s) {
        if (s.length() > 0 && s.charAt(s.length() - 1) != '\n')
            return s + "\n";
        return s;
    }

    public static void ensureEndIsNewLine(StringBuilder s) {
        if (s.length() > 0 && s.charAt(s.length() - 1) != '\n')
            s.append("\n");
    }

    public static String normalizeNewLine(String s) {
        s = s.replace("\r\n", "\n");
        s = s.replace("\r", "\n");
        return s;
    }

    public static String[] split(String s, String delimiter) {
        StringTokenizer tk = new StringTokenizer(s, delimiter);
        List<String> list = new ArrayList<>();
        while (tk.hasMoreTokens()) {
            list.add(tk.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    public static String wrap(String s, int width) {
        StringBuilder sb = new StringBuilder();
        int charCount = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n' || c == '\r') {
                charCount = 0;
            } else {
                charCount++;
                if (charCount > width) {
                    charCount = 0;
                    sb.append('\n');
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String truncate(String s, int maxLength) {
        if (s.length() > maxLength) {
            StringBuilder sb = new StringBuilder();
            sb.append(s.substring(0, maxLength - 3));
            sb.append("...");
            return sb.toString();
        }
        return s;
    }

    public static String trimHeadWhitespace(String s) {
        int start = 0;
        if (s.length() > 0 && s.charAt(0) == ' ') {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) != ' ')
                    break;
                start++;
            }
        }
        if (start < s.length())
            return s.substring(start, s.length());
        else
            return "";
    }

    public static String trimTailWhitespace(String s) {
        int end = s.length();
        if (s.length() > 0 && s.charAt(s.length() - 1) == ' ') {
            for (int i = s.length() - 1; i >= 0; i--) {
                if (s.charAt(i) != ' ')
                    break;
                end--;
            }
        }
        return s.substring(0, end);
    }

    // parse comma separaeted int array.
    public static int[] parseIntArray(String str) {
        String[] strValues = split(str, ",");
        int[] values = new int[strValues.length];
        for (int i = 0; i < values.length; i++) {
            String s = strValues[i].trim();
            values[i] = Integer.valueOf(s);
        }
        return values;
    }

    public static String toPlatformLineSeparator(String s) {
        return toPlatformLineSeparator(s, "\n");
    }

    public static String toPlatformLineSeparator(String s, String originalSeparator) {
        String separator = System.lineSeparator();
        if (!separator.equals(originalSeparator)) {
            s = s.replace(originalSeparator, separator);
        }
        return s;
    }

    public static String removeEndNewLine(String s) {
        return STRING_END_NEWLINE_PATTERN.matcher(s).replaceAll("");
    }

    public static String removeLastSegment(String path) {
        int index = path.lastIndexOf("/");
        if (0 <= index && index < path.length() - 1)
            return path.substring(0, index + 1);
        else
            return path;
    }

    public static String getExtension(String path) {
        int index = path.lastIndexOf(".");
        if (index >= 0) {
            return path.substring(index + 1, path.length());
        }
        return "";
    }

    public static String replaceAll(String str, Pattern pattern, MatchResultProcessor processor) {
        StringBuilder sb = new StringBuilder(str.length()*2);
        Matcher m = pattern.matcher(str);
        int loc = 0;
        while (m.find()) {
            sb.append(str.substring(loc, m.start()));
            loc = m.end();
            sb.append(processor.process(m));
        }

        sb.append(str.substring(loc, str.length()));
        return sb.toString();
    }

    public static String digest(String str) {
        try {
            byte[] buf = MessageDigest.getInstance("MD5").digest(StringUtils.getBytes(str, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : buf) {
                int i = b & 0xff;
                sb.append(Integer.toHexString(i).toUpperCase());
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat f = new SimpleDateFormat();
        return f.format(date);
    }

    public static String removeObjectReplacementCharacter(String str) {
        str = str.replace("" + UNICODE_OBJECT_REPLACEMENT_0, "");
        str = str.replace("" + UNICODE_OBJECT_REPLACEMENT_1, "");
        return str;
    }
}

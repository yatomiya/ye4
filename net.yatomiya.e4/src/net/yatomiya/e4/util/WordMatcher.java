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

public class WordMatcher {
    public static enum Mode {
        PLAIN,
        WILDCARD,
        REGEX,
    }

    private String text;
    private Mode mode;
    private boolean isCaseInsensitive;
    private boolean isNormalize;
    private boolean isMultiword;
    private boolean isAndMatchWhenMultiword;

    private List<Pattern> patternList;

    public WordMatcher() {
        this(Mode.PLAIN, "");
    }

    public WordMatcher(Mode mode, String text) {
        this(mode, text, true, true, true);
    }

    public WordMatcher(Mode mode, String text, boolean isCaseInsensitive, boolean isNormalize, boolean isMultiword) {
        this.mode = mode;
        this.text = text;
        this.isCaseInsensitive = isCaseInsensitive;
        this.isNormalize = isNormalize;
        this.isMultiword = isMultiword;
        isAndMatchWhenMultiword = true;

        buildPatternList();
    }

    private void buildPatternList() {
        String str = text;

        if (isNormalize) {
            str = StringUtils.halfToFull(str);
        }

        List<String> wordList = new ArrayList<>();
        if (isMultiword) {
            wordList.addAll(Arrays.asList(str.split("[ 　]+")));
        } else {
            wordList.add(str);
        }

        patternList = new ArrayList<>();
        for (String s : wordList) {
            patternList.add(createPattern(s));
        }
    }

    private Pattern createPattern(String word) {
        String str = word;

        if (mode == Mode.PLAIN) {
            str = Pattern.quote(str);
        } else if (mode == Mode.WILDCARD) {
            StringBuilder sb = new StringBuilder(str.length() * 2);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '*' || c == '＊') {
                    list.add(Pattern.quote(sb.toString()));
                    sb = new StringBuilder();
                    list.add(".*");
                } else if (c == '?' || c == '？') {
                    list.add(Pattern.quote(sb.toString()));
                    sb = new StringBuilder();
                    list.add(".?");
                } else {
                    sb.append(c);
                }
            }
            list.add(Pattern.quote(sb.toString()));

            sb = new StringBuilder();
            for (String s : list) {
                sb.append(s);
            }
            str = sb.toString();
        }

        if (isCaseInsensitive) {
            str = "(?iu)" + str;
        }

        Pattern p = null;
        try {
            p = Pattern.compile(str);
        } catch (PatternSyntaxException e) {
            p = Pattern.compile("");
        }
        return p;
    }

    public void setMode(Mode v) {
        if (mode != v) {
            mode = v;
            buildPatternList();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setText(String v) {
        if (!text.equals(v)) {
            text = v;
            buildPatternList();
        }
    }

    public String getText() {
        return text;
    }

    public void setCaseInsensitive(boolean v) {
        if (isCaseInsensitive != v) {
            isCaseInsensitive = v;
            buildPatternList();
        }
    }

    public boolean isCaseInsensitive() {
        return isCaseInsensitive;
    }

    public void setNormalize(boolean v) {
        if (isNormalize != v) {
            isNormalize = v;
            buildPatternList();
        }
    }

    public boolean isNormalize() {
        return isNormalize;
    }

    public void setMultiword(boolean v) {
        if (isMultiword != v) {
            isMultiword = v;
            buildPatternList();
        }
    }

    public boolean isMultiword() {
        return isMultiword;
    }

    public boolean match(String str) {
        if (text.equals(""))
            return false;

        if (isNormalize) {
            str = StringUtils.halfToFull(str);
        }

        for (Pattern p : patternList) {
            boolean found = p.matcher(str).find();
            if (isAndMatchWhenMultiword) {
                if (!found)
                    return false;
            } else {
                if (found)
                    break;
            }
        }

        return true;
    }

    public MatchResult[] find(String str) {
        if (text.equals(""))
            return new MatchResult[0];

        if (isNormalize) {
            str = StringUtils.halfToFull(str);
        }

        List<MatchResult> list = new ArrayList<>();
        for (Pattern p : patternList) {
            Matcher m = p.matcher(str);
            while (m.find()) {
                list.add(m.toMatchResult());
            }
        }

        if (patternList.size() > 1) {
            Collections.sort(
                list,
                new Comparator<MatchResult>() {
                    @Override
                    public int compare(MatchResult o1, MatchResult o2) {
                        if (o1.start() != o2.start())
                            return o1.start() - o2.start();
                        return o1.group().length() - o2.group().length();
                    }
                });
        }

        return list.toArray(new MatchResult[list.size()]);
    }

    public String replace(String str, String replaceStr) {
        if (text.equals(""))
            return str;

        for (Pattern p : patternList) {
            str = p.matcher(str).replaceAll(replaceStr);
        }
        return str;
    }

    public String replaceWithChar(String str, char replaceChar) {
        if (text.equals(""))
            return str;

        StringBuilder sb = new StringBuilder(str);
        for (MatchResult r : find(str)) {
            for (int i = r.start(); i < r.end(); i++) {
                sb.setCharAt(i, replaceChar);
            }
        }
        return sb.toString();
    }
}


/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import net.yatomiya.e4.ui.style.viewer.*;
import net.yatomiya.e4.util.*;

public class StyleAttribute {
    public static final String DEFAULT_VALUE = "default";

    private String name;
    private boolean isCascadable;
    private boolean isPresentational;

    public StyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
        this.name = name;
        this.isCascadable = isCascadable;
        this.isPresentational = isPresentational;
    }

    public String getName() {
        return name;
    }

    public boolean isCascadable() {
        return isCascadable;
    }

    public boolean isPresentational() {
        return isPresentational;
    }

    public Object parseValue(String value) {
        return value;
    }

    public Object evaluateContextValue(Object value, Object contextValue) {
        return value;
    }

    public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
        return false;
    }

    public boolean applyLineAttribute(LineAttribute line, Object attrValue, StyleNode node, StyleViewer viewer) {
        return false;
    }

    public static final StyleAttribute ID = new StyleAttribute("id", false, false);
    public static final StyleAttribute VALUE = new StyleAttribute("value", false, false);
    public static final StyleAttribute INDENT = new IntegerStyleAttribute("indent", true, true) {
            @Override
            public Object evaluateContextValue(Object value, Object contextValue) {
                int parentIndent = 0;
                if (contextValue != null)
                    parentIndent = (Integer)contextValue;
                int newIndent = parentIndent + (Integer)value;
                return newIndent;
            }

            @Override
            public boolean applyLineAttribute(LineAttribute line, Object attrValue, StyleNode node, StyleViewer viewer) {
                line.indent = (Integer)attrValue;
                return true;
            }
        };
    public static final StyleAttribute COLOR = new ColorStyleAttribute("color", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.foreground = viewer.getResourceManager().getColor((RGB)attrValue);
                return true;
            }
        };
    public static final StyleAttribute BACKGROUND_COLOR = new ColorStyleAttribute("background_color", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.background = viewer.getResourceManager().getColor((RGB)attrValue);
                return true;
            }
        };
    public static final StyleAttribute FONT = new StyleAttribute("font", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                applyFontStyleRange(range, this, attrValue, node, viewer);
                return true;
            }
        };
    public static final StyleAttribute FONT_SIZE = new StyleAttribute("font_size", true, true) {
            @Override
            public Object parseValue(String value) {
                String str = value;
                if (str.length() == 0)
                    return null;

                boolean fixed = true;
                int size = 0;
                if (str.charAt(0) == '+' || str.charAt(0) == '-') {
                    fixed = false;
                }
                try {
                    size = Integer.valueOf(str);
                } catch (NumberFormatException e) {
                    return null;
                }

                Object[] data = new Object[2];
                data[0] = fixed;
                data[1] = size;
                return data;
            }

            @Override
            public Object evaluateContextValue(Object value, Object contextValue) {
                boolean parentFixed = (Boolean)((Object[])contextValue)[0];
                int parentSize = (Integer)((Object[])contextValue)[1];
                boolean fixed = (Boolean)((Object[])value)[0];
                int size = (Integer)((Object[])value)[1];
                if (!fixed) {
                    fixed = parentFixed;
                    size += parentSize;
                }

                Object[] data = new Object[2];
                data[0] = fixed;
                data[1] = size;
                return data;
            }

            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                applyFontStyleRange(range, this, attrValue, node, viewer);
                return true;
            }
        };
    public static final StyleAttribute FONT_STYLE = new StyleAttribute("font_style", true, true) {
            @Override
            public Object parseValue(String value) {
                Object attrValue = null;
                if (value.equals("normal"))
                    attrValue = Boolean.FALSE;
                else if (value.equals("italic"))
                    attrValue = Boolean.TRUE;
                return attrValue;
            }

            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                applyFontStyleRange(range, this, attrValue, node, viewer);
                return true;
            }
        };
    public static final StyleAttribute FONT_WEIGHT = new StyleAttribute("font_weight", true, true) {
            @Override
            public Object parseValue(String value) {
                Object attrValue = null;
                if (value.equals("normal"))
                    attrValue = Boolean.FALSE;
                else if (value.equals("bold"))
                    attrValue = Boolean.TRUE;
                return attrValue;
            }

            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                applyFontStyleRange(range, this, attrValue, node, viewer);
                return true;
            }
        };
    public static final StyleAttribute STRIKEOUT = new BooleanStyleAttribute("strikeout", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.strikeout = (Boolean)attrValue;
                return true;
            }
        };
    public static final StyleAttribute UNDERLINE = new BooleanStyleAttribute("underline", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.underline = (Boolean)attrValue;
                return true;
            }
        };
    public static final StyleAttribute METRICS = new MetricsStyleAttribute("metrics", true, true);
    public static final StyleAttribute ONUPDATE = new StyleAttribute("onupdate", false, false);
    public static final StyleAttribute ONENTER = new StyleAttribute("onenter", false, false);
    public static final StyleAttribute ONEXIT = new StyleAttribute("onexit", false, false);
    public static final StyleAttribute ONMOVE = new StyleAttribute("onmove", false, false);
    public static final StyleAttribute ONCLICK = new StyleAttribute("onclick", false, false);
    public static final StyleAttribute ONHOVER = new StyleAttribute("onhover", false, false);
    public static final StyleAttribute CURSOR = new StyleAttribute("cursor", true, false);
    public static final StyleAttribute HREF = new StyleAttribute("href", true, false);

    private static StyleAttribute[] standardAttributes = new StyleAttribute[] {
        ID,
        VALUE,
        INDENT,
        COLOR,
        BACKGROUND_COLOR,
        FONT,
        FONT_SIZE,
        FONT_STYLE,
        FONT_WEIGHT,
        STRIKEOUT,
        UNDERLINE,
        METRICS,
        ONUPDATE,
        ONENTER,
        ONEXIT,
        ONMOVE,
        ONCLICK,
        ONHOVER,
        CURSOR,
        HREF,
    };

    public static StyleAttribute[] getStandardAttributes() {
        return standardAttributes.clone();
    }

    public static void applyFontStyleRange(StyleRange range, StyleAttribute attr, Object attrValue, StyleNode node, StyleViewer viewer) {
        FontData currentData = null;
        if (range.font != null) {
            currentData = range.font.getFontData()[0];
        } else {
            currentData = viewer.getPropotionalFont().getFontData()[0];
        }
        if (currentData == null)
            return;

        String fontName = currentData.getName();
        int fontSize = currentData.getHeight();
        int fontStyle = currentData.getStyle();
        if (attr == FONT) {
            fontName = (String)attrValue;

            if (fontName.equals("propotional")) {
                fontName = viewer.getPropotionalFont().getFontData()[0].getName();
            } else if (fontName.equals("monospace")) {
                fontName = viewer.getMonospaceFont().getFontData()[0].getName();
            }
        } else if (attr == FONT_SIZE) {
            Object[] data = (Object[])attrValue;
            boolean fixed = (Boolean)data[0];
            int size = (Integer)data[1];
            if (fixed) {
                fontSize = size;
            } else {
                fontSize += size;
            }
        } else if (attr == FONT_STYLE) {
            if ((Boolean)attrValue)
                fontStyle |= SWT.ITALIC;
            else
                fontStyle &= ~SWT.ITALIC;
        } else if (attr == FONT_WEIGHT) {
            if ((Boolean)attrValue)
                fontStyle |= SWT.BOLD;
            else
                fontStyle &= ~SWT.BOLD;
        }

        FontData newData = new FontData(fontName, fontSize, fontStyle);
        if (!currentData.equals(newData)) {
            range.font = viewer.getResourceManager().getFont(newData);
        }
    }


    public static class BooleanStyleAttribute extends StyleAttribute {
        public BooleanStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
            super(name, isCascadable, isPresentational);
        }

        @Override
        public Object parseValue(String value) {
            return Boolean.valueOf(value);
        }
    }

    public static class IntegerStyleAttribute extends StyleAttribute {
        public IntegerStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
            super(name, isCascadable, isPresentational);
        }

        @Override
        public Object parseValue(String value) {
            Integer attrValue = null;
            try {
                attrValue = Integer.valueOf(value);
            } catch (NumberFormatException e) {
            }
            return attrValue;
        }
    }

    public static class ColorStyleAttribute extends StyleAttribute {
        public ColorStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
            super(name, isCascadable, isPresentational);
        }

        @Override
        public Object parseValue(String value) {
            int[] nums = HtmlUtils.parseColor(value);
            return new RGB(nums[0], nums[1], nums[2]);
        }
    }

    public static class MetricsStyleAttribute extends StyleAttribute {
        public MetricsStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
            super(name, isCascadable, isPresentational);
        }

        @Override
        public Object parseValue(String value) {
            int[] nums = StringUtils.parseIntArray(value);
            if (nums.length < 2)
                throw new IllegalStateException("Metrics need width and ascent value.");
            int width = nums[0];
            int ascent = nums[1];
            int descent = 0;
            if (nums.length > 2)
                descent = nums[2];
            GlyphMetrics m = new GlyphMetrics(ascent, descent, width);
            return m;
        }


        @Override
        public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
            range.metrics = (GlyphMetrics)attrValue;
            return true;
        }
    }

}

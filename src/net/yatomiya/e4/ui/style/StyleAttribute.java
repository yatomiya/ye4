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

    public Object applyCascadeValue(Object value, Object cascadedValue) {
        return value;
    }

    public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
        return false;
    }

    public boolean applyLineAttribute(LineAttribute line, Object attrValue, StyleNode node, StyleViewer viewer) {
        return false;
    }

    public static final StyleAttribute ATTRIBUTE_ID = new StyleAttribute("id", false, false);
    public static final StyleAttribute ATTRIBUTE_INDENT = new IntegerStyleAttribute("indent", true, true) {
            @Override
            public Object applyCascadeValue(Object value, Object cascadedValue) {
                int parentIndent = 0;
                if (cascadedValue != null)
                    parentIndent = (Integer)cascadedValue;
                int newIndent = parentIndent + (Integer)value;
                return newIndent;
            }

            @Override
            public boolean applyLineAttribute(LineAttribute line, Object attrValue, StyleNode node, StyleViewer viewer) {
                line.indent = (Integer)attrValue;
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_COLOR = new ColorStyleAttribute("color", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.foreground = viewer.getResourceManager().getColor((RGB)attrValue);
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_BACKGROUND_COLOR = new ColorStyleAttribute("background_color", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.background = viewer.getResourceManager().getColor((RGB)attrValue);
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_FONT = new StyleAttribute("font", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                applyFontStyleRange(range, this, attrValue, node, viewer);
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_FONT_SIZE = new StyleAttribute("font_size", true, true) {
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
            public Object applyCascadeValue(Object value, Object cascadedValue) {
                boolean parentFixed = (Boolean)((Object[])cascadedValue)[0];
                int parentSize = (Integer)((Object[])cascadedValue)[1];
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
    public static final StyleAttribute ATTRIBUTE_FONT_STYLE = new StyleAttribute("font_style", true, true) {
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
    public static final StyleAttribute ATTRIBUTE_FONT_WEIGHT = new StyleAttribute("font_weight", true, true) {
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
    public static final StyleAttribute ATTRIBUTE_STRIKEOUT = new BooleanStyleAttribute("strikeout", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.strikeout = (Boolean)attrValue;
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_UNDERLINE = new BooleanStyleAttribute("underline", true, true) {
            @Override
            public boolean applyStyleRange(StyleRange range, Object attrValue, StyleNode node, StyleViewer viewer) {
                range.underline = (Boolean)attrValue;
                return true;
            }
        };
    public static final StyleAttribute ATTRIBUTE_METRICS = new MetricsStyleAttribute("metrics", true, true);
    public static final StyleAttribute ATTRIBUTE_ONENTER = new StyleAttribute("onenter", true, false);
    public static final StyleAttribute ATTRIBUTE_ONEXIT = new StyleAttribute("onexit", true, false);
    public static final StyleAttribute ATTRIBUTE_ONMOVE = new StyleAttribute("onmove", true, false);
    public static final StyleAttribute ATTRIBUTE_ONCLICK = new StyleAttribute("onclick", true, false);
    public static final StyleAttribute ATTRIBUTE_ONHOVER = new StyleAttribute("onhover", true, false);
    public static final StyleAttribute ATTRIBUTE_CURSOR = new StyleAttribute("cursor", true, false);

    public static final StyleAttribute ATTRIBUTE_HREF = new StyleAttribute("href", true, false);

    private static StyleAttribute[] standardAttributes = new StyleAttribute[] {
        ATTRIBUTE_ID,
        ATTRIBUTE_INDENT,
        ATTRIBUTE_COLOR,
        ATTRIBUTE_BACKGROUND_COLOR,
        ATTRIBUTE_FONT,
        ATTRIBUTE_FONT_SIZE,
        ATTRIBUTE_FONT_STYLE,
        ATTRIBUTE_FONT_WEIGHT,
        ATTRIBUTE_STRIKEOUT,
        ATTRIBUTE_UNDERLINE,
        ATTRIBUTE_METRICS,
        ATTRIBUTE_ONENTER,
        ATTRIBUTE_ONEXIT,
        ATTRIBUTE_ONMOVE,
        ATTRIBUTE_ONCLICK,
        ATTRIBUTE_ONHOVER,
        ATTRIBUTE_CURSOR,
        ATTRIBUTE_HREF,
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
        if (attr == ATTRIBUTE_FONT) {
            fontName = (String)attrValue;

            if (fontName.equals("propotional")) {
                fontName = viewer.getPropotionalFont().getFontData()[0].getName();
            } else if (fontName.equals("monospace")) {
                fontName = viewer.getMonospaceFont().getFontData()[0].getName();
            }
        } else if (attr == ATTRIBUTE_FONT_SIZE) {
            Object[] data = (Object[])attrValue;
            boolean fixed = (Boolean)data[0];
            int size = (Integer)data[1];
            if (fixed) {
                fontSize = size;
            } else {
                fontSize += size;
            }
        } else if (attr == ATTRIBUTE_FONT_STYLE) {
            if ((Boolean)attrValue)
                fontStyle |= SWT.ITALIC;
            else
                fontStyle &= ~SWT.ITALIC;
        } else if (attr == ATTRIBUTE_FONT_WEIGHT) {
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
}

class BooleanStyleAttribute extends StyleAttribute {
    public BooleanStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
        super(name, isCascadable, isPresentational);
    }

    @Override
    public Object parseValue(String value) {
        return Boolean.valueOf(value);
    }
}

class IntegerStyleAttribute extends StyleAttribute {
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

class ColorStyleAttribute extends StyleAttribute {
    public ColorStyleAttribute(String name, boolean isCascadable, boolean isPresentational) {
        super(name, isCascadable, isPresentational);
    }

    @Override
    public Object parseValue(String value) {
        int[] nums = HtmlUtils.hexToRgb(value);
        return new RGB(nums[0], nums[1], nums[2]);
    }
}

class MetricsStyleAttribute extends StyleAttribute {
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


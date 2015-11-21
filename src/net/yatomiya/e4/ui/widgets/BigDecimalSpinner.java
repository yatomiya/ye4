/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

import java.math.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;



public class BigDecimalSpinner extends Composite {
    private static final int BUTTON_WIDTH = 16;

    private Text text;

    private Button upButton;

    private Button downButton;

    private BigDecimal selection;

    private BigDecimal min;

    private BigDecimal max;

    private BigDecimal increment;

    private BigDecimal pageIncrement;

    private boolean intMode;

    private String backupText;

    private ButtonAutoRepeater upRepeater;

    private ButtonAutoRepeater downRepeater;

    private LocalLayout layout;

    class LocalLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed) {
            int width = 0;
            int height = 0;

            GC gc = new GC(text);
            Point maxExtent = gc.textExtent(max.toPlainString());
            Point minExtent = gc.textExtent(min.toPlainString());
            gc.dispose();

            Point textExtent = maxExtent;
            if (minExtent.x > maxExtent.x)
                textExtent.x = minExtent.x;
            if (minExtent.y > maxExtent.y)
                textExtent.y = minExtent.y;

            Point pt = text.computeSize(textExtent.x, textExtent.y);
            width = pt.x + BUTTON_WIDTH;
            height = pt.y;

            if (wHint != SWT.DEFAULT) {
                if (width < wHint)
                    width = wHint;
            }

            return new Point(width, height);
        }

        @Override
        protected void layout(Composite composite, boolean flushCached) {
            Rectangle bounds = getBounds();
            int textWidth = bounds.width - BUTTON_WIDTH;
            int buttonHeight = bounds.height / 2;
            text.setBounds(0, 0, textWidth, bounds.height);
            upButton.setBounds(textWidth, 0, BUTTON_WIDTH, buttonHeight);
            downButton.setBounds(textWidth, bounds.height - buttonHeight, BUTTON_WIDTH, buttonHeight);
        }
    }

    public BigDecimalSpinner(Composite parent, int style,
                             BigDecimal selection, BigDecimal min, BigDecimal max,
                             BigDecimal increment, BigDecimal pageIncrement,
                             boolean intMode) {
        super(parent, style);

        text = new Text(this, style | SWT.SINGLE | SWT.BORDER);
        upButton = new Button(this, style | SWT.ARROW | SWT.UP);
        downButton = new Button(this, style | SWT.ARROW | SWT.DOWN);

        layout = new LocalLayout();
        super.setLayout(layout);

        text.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                verify(e);
            }
        });
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                textEntered(e);
            }
        });
        text.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                backupText = ((Text)event.widget).getText();
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (!backupText.equals(((Text)event.widget).getText())) {
                    event.widget.notifyListeners(SWT.DefaultSelection, new Event());
                }
            }
        });
        text.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent event) {
                traverse(event);
            }
        });

        upRepeater = new ButtonAutoRepeater(upButton) {
            @Override
            void buttonDown() {
                up();
            }
        };

        downRepeater = new ButtonAutoRepeater(downButton) {
            @Override
            void buttonDown() {
                down();
            }
        };

        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                layout();
            }
        });

        addListener(SWT.FocusIn, new Listener() {
            @Override
            public void handleEvent(Event e) {
                text.setFocus();
            }
        });

        text.setFont(getFont());

        this.min = min;
        this.max = max;
        this.increment = increment;
        this.pageIncrement = pageIncrement;
        this.intMode = intMode;

        setSelection(selection);
    }

    public BigDecimalSpinner(Composite parent, int style,
                             int selection, int min, int max,
                             int increment, int pageIncrement) {
        this(parent, style, new BigDecimal(selection), new BigDecimal(min), new BigDecimal(max),
             new BigDecimal(increment), new BigDecimal(pageIncrement), true);
    }

    public BigDecimalSpinner(Composite parent, int style) {
        this(parent, style, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 10);
    }

    public Text getText() {
        return text;
    }

    @Override
    public void setEnabled(boolean v) {
        super.setEnabled(v);

        text.setEnabled(v);
        upButton.setEnabled(v);
        downButton.setEnabled(v);
    }

    private void verify(VerifyEvent e) {
        StringBuilder sb = new StringBuilder(((Text)e.widget).getText());
        sb.replace(e.start, e.end + 1, e.text);
        String newText = new String(sb);

        if (newText.length() == 0
            || (!intMode && newText.matches("[\\d\\-\\.]+"))
            || (intMode && newText.matches("[\\d\\-]+")))
            e.doit = true;
        else
            e.doit = false;
    }

    private void textEntered(SelectionEvent e) {
        String str = ((Text)e.widget).getText();
        if (str.length() == 0)
            str = backupText;

        BigDecimal v;
        boolean force = false;
        boolean updateText = false;

        try {
            v = new BigDecimal(str);
        } catch (NumberFormatException ex) {
            try {
                v = new BigDecimal(backupText);
            } catch (NumberFormatException exx) {
                v = min;
                force = updateText = true;
            }
        }

        updateSelection(v, force, updateText, true);
    }

    private void traverse(TraverseEvent e) {
        switch (e.detail) {
        case SWT.TRAVERSE_ARROW_PREVIOUS:
            if (e.keyCode == SWT.ARROW_UP) {
                e.doit = true;
                e.detail = SWT.NULL;
                up();
            }
            break;
        case SWT.TRAVERSE_ARROW_NEXT:
            if (e.keyCode == SWT.ARROW_DOWN) {
                e.doit = true;
                e.detail = SWT.NULL;
                down();
            }
            break;
        }
    }

    private void up() {
        updateSelection(getSelection().add(increment), false, true, true);
    }

    private void down() {
        updateSelection(getSelection().subtract(increment), false, true, true);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        text.setFont(font);
    }

    public void setSelection(BigDecimal v) {
        updateSelection(v, true, true, false);
    }

    private void updateSelection(BigDecimal newValue, boolean force, boolean updateText, boolean notifyEvent) {
        if (newValue.compareTo(min) < 0) {
            newValue = min;
        } else if (newValue.compareTo(max) > 0) {
            newValue = max;
        }

        if (!force && (selection.compareTo(newValue) == 0))
            return;

        selection = newValue;

        if (updateText) {
            String newText;
            if (intMode) {
                newText = String.valueOf(selection.intValue());
            } else {
                newText = selection.toPlainString();
            }
            text.setText(newText);
        }

        if (notifyEvent)
            text.notifyListeners(SWT.DefaultSelection, new Event());
    }

    public BigDecimal getSelection() {
        return selection;
    }

    public void setMaximum(BigDecimal max) {
        this.max = max;
        updateSelection(selection, true, true, false);
    }

    public BigDecimal getMaximum() {
        return max;
    }

    public void setMinimum(BigDecimal min) {
        this.min = min;
        updateSelection(selection, true, true, false);
    }

    public BigDecimal getMinimum() {
        return min;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setPageIncrement(BigDecimal pageIncrement) {
        this.pageIncrement = pageIncrement;
    }

    public BigDecimal getPageIncrement() {
        return pageIncrement;
    }

    public void setIntMode(boolean v) {
        intMode = v;
    }

    public boolean getIntMode() {
        return intMode;
    }

    @Override
    public void setLayout(Layout layout) {
        throw new UnsupportedOperationException();
    }

    public void addSelectionListener(SelectionListener listener) {
        if (listener == null)
            throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
        text.addSelectionListener(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        text.removeSelectionListener(listener);
    }

    abstract class ButtonAutoRepeater implements Listener {
        private boolean repeating;

        private Button button;

        class Repeater implements Runnable {
            boolean enabled = true;

            @Override
            public void run() {
                if (enabled) {
                    buttonDown();
                    button.getDisplay().timerExec(50, this);
                }
            }
        }

        Repeater repeater;

        ButtonAutoRepeater(Button button) {
            this.button = button;

            button.addListener(SWT.MouseDown, this);
            button.addListener(SWT.MouseUp, this);
            button.addListener(SWT.FocusOut, this);
            button.addListener(SWT.Dispose, this);
        }

        abstract void buttonDown();

        @Override
        public void handleEvent(Event e) {
            switch (e.type) {
            case SWT.MouseDown:
                if (repeater != null) {
                    repeater.enabled = false;
                    repeater = null;
                }

                buttonDown();

                repeater = new Repeater();
                button.getDisplay().timerExec(500, repeater);
                break;

            case SWT.MouseUp:
            case SWT.FocusOut:
            case SWT.Dispose:
                if (repeater != null) {
                    repeater.enabled = false;
                    repeater = null;
                }
                break;
            }
        }
    }


}


/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;


import java.util.*;
import java.util.List;
import java.util.regex.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.*;
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public abstract class SearchCombo<E> extends Composite {
    protected static final int MAX_HISTORY_COUNT = 16;

    protected CCombo combo;
    protected Button menuButton;
    protected Button previousButton;
    protected Button nextButton;
    protected Button clearButton;

    private WordMatcher matcher;
    private boolean isFiltering;
    private boolean isIncremental;
    private String searchingText;
    private List<E> foundElements;
    private int foundElementIndex;

    public SearchCombo(Composite parent) {
        super(parent, SWT.NONE);

        matcher = new WordMatcher();
        isFiltering = false;
        isIncremental = false;

        searchingText = "";
        foundElements = new ArrayList<>();
        foundElementIndex = -1;

        LinearLayout layout = new LinearLayout();
        setLayout(layout);
        LinearData data;

        combo = new CCombo(this, SWT.DROP_DOWN);
        data = new LinearData(SWT.FILL, SWT.CENTER, true, false);
        combo.setLayoutData(data);
        combo.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (isIncremental()) {
                        search();
                    }
                }
            });
        combo.addSelectionListener(new SelectionListener() {
                // item selected
                @Override
                public void widgetSelected(SelectionEvent e) {
                    search();
                }

                // text entered.
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    if (searchingText.equals(combo.getText()))
                        nextSelected();
                    else
                        search();
                }
            });
        CComboHistory.configure(combo, 16, getHistoryPersistenceKey());

        clearButton = new Button(this, SWT.PUSH);
        clearButton.setText("C");
        clearButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    combo.setText("");
                    search();
                }
            });

        previousButton = new Button(this, SWT.ARROW | SWT.LEFT);
        previousButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    previousSelected();
                }
            });

        nextButton = new Button(this, SWT.ARROW | SWT.RIGHT);
        nextButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    nextSelected();
                }
            });

        menuButton = new Button(this, SWT.ARROW | SWT.DOWN);
        menuButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    popupMenu(menuButton);
                }
            });

    }

    public void search() {
        String v = combo.getText();
        searchingText = v;

        matcher.setText(v);

        foundElements.clear();
        foundElementIndex = -1;
        foundElements.addAll(searchElements(v));
        if (foundElements.size() > 0) {
            foundElementIndex = 0;
        }

        refreshViewer(Collections.unmodifiableList(foundElements));

        doShowFoundElement();
    }

    private void doShowFoundElement() {
        if (foundElements.size() > 0) {
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    protected abstract List<E> searchElements(String text);
    protected abstract void refreshViewer(List<E> foundElements);
    protected abstract void showFoundElement(E element);

    protected void nextSelected() {
        if (foundElements.size() > 0) {
            foundElementIndex = JUtils.clampLoop(foundElementIndex + 1, 0, foundElements.size() - 1);
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    protected void previousSelected() {
        if (foundElements.size() > 0) {
            foundElementIndex = JUtils.clampLoop(foundElementIndex - 1, 0, foundElements.size() - 1);
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    public List<Object> getFoundElements() {
        return Collections.unmodifiableList(foundElements);
    }

    public void setText(String v) {
        combo.setText(v);
        matcher.setText(v);
    }

    public String getText() {
        return combo.getText();
    }

    public WordMatcher.Mode getMode() {
        return matcher.getMode();
    }

    public void setMode(WordMatcher.Mode v) {
        if (matcher.getMode() != v) {
            matcher.setMode(v);
        }
    }

    public void setFiltering(boolean v) {
        isFiltering = v;
    }

    public boolean isFiltering() {
        return isFiltering;
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean value) {
        isIncremental = value;
    }

    public boolean isCaseInsensitive() {
        return matcher.isCaseInsensitive();
    }

    public void setCaseInsensitive(boolean value) {
        if (isCaseInsensitive() != value) {
            matcher.setCaseInsensitive(value);
        }
    }

    public boolean isNormalize() {
        return matcher.isNormalize();
    }

    public void setNormalize(boolean v) {
        if (isNormalize() != v) {
            matcher.setNormalize(v);
        }
    }

    public boolean isMultiword() {
        return matcher.isMultiword();
    }

    public void setMultiword(boolean v) {
        if (isMultiword() != v) {
            matcher.setMultiword(v);
        }
    }

    public boolean check(String str) {
        return matcher.check(str);
    }

    public MatchResult[] find(String str) {
        return matcher.find(str);
    }

    protected abstract String getHistoryPersistenceKey();

    protected List<String> getHistoryStrings() {
        return AppUtils.get(PersistenceService.class).
            get(getHistoryPersistenceKey(), () -> new ArrayList<String>());
    }

    void popupMenu(Control parent) {
        Menu menu = new Menu(getShell(), SWT.POP_UP);

        MenuItem item;

        MenuItem plainItem = new MenuItem(menu, SWT.CHECK);
        plainItem.setText("プレーンモード");
        plainItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMode(WordMatcher.Mode.PLAIN);
                    search();
                }
            });
        MenuItem wildItem = new MenuItem(menu, SWT.CHECK);
        wildItem.setText("ワイルドカードモード");
        wildItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMode(WordMatcher.Mode.WILDCARD);
                    search();
                }
            });
        MenuItem regexItem = new MenuItem(menu, SWT.CHECK);
        regexItem.setText("正規表現モード");
        regexItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMode(WordMatcher.Mode.REGEX);
                    search();
                }
            });
        switch (getMode()) {
            case PLAIN:
                plainItem.setSelection(true);
                break;
            case WILDCARD:
                wildItem.setSelection(true);
                break;
            case REGEX:
                regexItem.setSelection(true);
                break;
        }

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("インクリメンタル検索");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setIncremental(!isIncremental());
                }
            });
        item.setSelection(isIncremental());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("フィルタリング");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setFiltering(!isFiltering());
                    search();
                }
            });
        item.setSelection(isFiltering());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("小文字/大文字一致");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setCaseInsensitive(!isCaseInsensitive());
                    search();
                }
            });
        item.setSelection(isCaseInsensitive());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("半角/全角一致");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setNormalize(!isNormalize());
                    search();
                }
            });
        item.setSelection(isNormalize());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("マルチワード");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMultiword(!isMultiword());
                    search();
                }
            });
        item.setSelection(isMultiword());

        Rectangle rect = parent.getBounds();
        Point loc = new Point(rect.x - 1, rect.y + rect.height);
        menu.setLocation(getShell().getDisplay().map(parent.getParent(), null, loc));
        menu.setVisible(true);
    }

}


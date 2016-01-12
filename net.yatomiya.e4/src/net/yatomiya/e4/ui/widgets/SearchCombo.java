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
import net.yatomiya.e4.services.*;
import net.yatomiya.e4.util.*;

public abstract class SearchCombo<E> extends Composite {
    protected static final int MAX_HISTORY_COUNT = 16;

    protected CCombo textCombo;
    protected Button searchButton;
    protected Button clearButton;
    protected Button previousButton;
    protected Button nextButton;
    protected Button menuButton;

    protected WordMatcher matcher;
    protected boolean isFiltering;
    protected boolean isIncremental;
    protected boolean doSearchWhenMatchConditionChanged;

    protected String searchText;
    protected List<E> foundElements;
    protected int foundElementIndex;

    boolean ignoreTextComboModify;

    public SearchCombo(Composite parent) {
        this(parent, WordMatcher.Mode.PLAIN, true, true, true);
    }

    public SearchCombo(Composite parent, WordMatcher.Mode mode, boolean isCaseInsensitive, boolean isNormalize, boolean isMultiword) {
        super(parent, SWT.NONE);

        matcher = new WordMatcher(mode, "", isCaseInsensitive, isNormalize, isMultiword);
        isFiltering = false;
        isIncremental = false;
        doSearchWhenMatchConditionChanged = false;

        searchText = "";
        foundElementIndex = -1;
        foundElements = new ArrayList<>();

        LinearLayout layout = new LinearLayout();
        setLayout(layout);
        LinearData data;

        textCombo = new CCombo(this, SWT.DROP_DOWN);
        data = new LinearData(SWT.FILL, SWT.CENTER, true, false);
        textCombo.setLayoutData(data);
        textCombo.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (ignoreTextComboModify)
                        return;

                    matcher.setText(getText());

                    if (isIncremental()) {
                        doIncrementalSearch();
                    }
                }
            });
        textCombo.addSelectionListener(new SelectionListener() {
                // item selected
                @Override
                public void widgetSelected(SelectionEvent e) {
                    doSearchPressed();
                }

                // text entered.
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    if (isSearching() && searchText.equals(getText())) {
                        doNextPressed();
                    } else {
                        doSearchPressed();
                    }
                }
            });
        CComboHistory.configure(textCombo, 16, getHistoryPersistenceKey());

        searchButton = new Button(this, SWT.PUSH);
        searchButton.setText("S");
        searchButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    clearSearchState();
                    doSearchPressed();
                }
            });

        clearButton = new Button(this, SWT.PUSH);
        clearButton.setText("C");
        clearButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setText("");
                    doClearPressed();
                }
            });

        previousButton = new Button(this, SWT.ARROW | SWT.LEFT);
        previousButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    doPreviousPressed();
                }
            });

        nextButton = new Button(this, SWT.ARROW | SWT.RIGHT);
        nextButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    doNextPressed();
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

    public void clear() {
        setText("");
        clearSearchState();
    }

    public void clearSearchState() {
        List<E> oldFoundElements = new ArrayList<>(foundElements);
        String oldSearchText = searchText;

        searchText = "";
        foundElementIndex = -1;

        foundElements.clear();

        updatePresentation();
    }

    public void updatePresentation() {
    }

    public boolean isSearching() {
        return foundElementIndex >= 0;
    }

    protected void doIncrementalSearch() {
        if (JUtils.isEmpty(getText())) {
            clearSearchState();
        } else {
            search();
        }
    }

    protected void doSearchPressed() {
        if (JUtils.isEmpty(getText()))
            clearSearchState();
        else
            search();
    }

    protected void doClearPressed() {
        clear();
    }

    protected void doNextPressed() {
        selectNextFoundElement();
    }

    protected void doPreviousPressed() {
        selectPreviousFoundElement();
    }

    public void search() {
        String v = getText();
        searchText = v;

        List<E> oldElements = new ArrayList<>(foundElements);

        foundElements.clear();
        foundElements.addAll(searchElements(v));
        foundElementIndex = -1;
        if (foundElements.size() > 0)
            foundElementIndex = 0;

        updatePresentation();

        if (foundElements.size() > 0) {
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    protected List<E> searchElements(String text) {
        return Collections.EMPTY_LIST;
    }

    protected void showFoundElement(E element) {
    }

    public void selectNextFoundElement() {
        if (foundElements.size() > 0) {
            foundElementIndex = JUtils.clampLoop(foundElementIndex + 1, 0, foundElements.size() - 1);
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    public void selectPreviousFoundElement() {
        if (foundElements.size() > 0) {
            foundElementIndex = JUtils.clampLoop(foundElementIndex - 1, 0, foundElements.size() - 1);
            showFoundElement(foundElements.get(foundElementIndex));
        }
    }

    public List<E> getFoundElements() {
        return Collections.unmodifiableList(foundElements);
    }

    public void setText(String v) {
        ignoreTextComboModify = true;
        textCombo.setText(v);
        ignoreTextComboModify = false;

        matcher.setText(v);
    }

    public String getText() {
        return textCombo.getText();
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

    public boolean match(String str) {
        return matcher.match(str);
    }

    public MatchResult[] find(String str) {
        return matcher.find(str);
    }

    protected abstract String getHistoryPersistenceKey();

    protected List<String> getHistoryStrings() {
        return EUtils.get(PersistenceService.class)
            .get(getHistoryPersistenceKey(), () -> new ArrayList<String>());
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
                    doPopupSearch();
                }
            });
        MenuItem wildItem = new MenuItem(menu, SWT.CHECK);
        wildItem.setText("ワイルドカードモード");
        wildItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMode(WordMatcher.Mode.WILDCARD);
                    doPopupSearch();
                }
            });
        MenuItem regexItem = new MenuItem(menu, SWT.CHECK);
        regexItem.setText("正規表現モード");
        regexItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMode(WordMatcher.Mode.REGEX);
                    doPopupSearch();
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
                    doPopupSearch();
                }
            });
        item.setSelection(isFiltering());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("小文字/大文字一致");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setCaseInsensitive(!isCaseInsensitive());
                    doPopupSearch();
                }
            });
        item.setSelection(isCaseInsensitive());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("半角/全角一致");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setNormalize(!isNormalize());
                    doPopupSearch();
                }
            });
        item.setSelection(isNormalize());

        item = new MenuItem(menu, SWT.CHECK);
        item.setText("マルチワード");
        item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    setMultiword(!isMultiword());
                    doPopupSearch();
                }
            });
        item.setSelection(isMultiword());

        Rectangle rect = parent.getBounds();
        Point loc = new Point(rect.x - 1, rect.y + rect.height);
        menu.setLocation(getShell().getDisplay().map(parent.getParent(), null, loc));
        menu.setVisible(true);
    }

    void doPopupSearch() {
        if (doSearchWhenMatchConditionChanged)
            doSearchPressed();
    }
}


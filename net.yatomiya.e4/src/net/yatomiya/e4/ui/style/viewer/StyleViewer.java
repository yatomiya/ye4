/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.ui.viewer.*;
import net.yatomiya.e4.util.*;

public class StyleViewer extends ProjectionViewer {
    public static final String EVENT_FUNC_OPEN_URL = "open_url";

    private UIResourceManager resMgr;
    private Font propotionalFont;
    private Font monospaceFont;
    private IVerticalRuler verticalRuler;
    private IOverviewRuler overviewRuler;
    private ProjectionSupport projSupport;
    private IDocumentListener documentListener;
    private StyleNodeVisibleEventChecker nodeVisibleChecker;
    private AnnotationModel rootAnnotationModel;
    private AnnotationPainterManager annotationPainterManager;
    private StyleMouseManager mouseManager;
    private PopupWindowManager popupManager;
    private Map<String, EventFunction> funcMap;

    public StyleViewer(Composite parent) {
        // WRAP を指定すると H_SCROLL は表示されない。
        this(parent, SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
    }

    public StyleViewer(Composite parent, int style) {
        this(parent, style, new CompositeRuler(), new InternalOverviewRuler());
    }

    public StyleViewer(Composite parent, int style, IVerticalRuler vruler, IOverviewRuler oruler) {
        super(parent, vruler, oruler, true, style);

        this.verticalRuler = vruler;
        this.overviewRuler = oruler;

        documentListener = new IDocumentListener() {
                @Override
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                @Override
                public void documentChanged(DocumentEvent event) {
                }
            };

        addTextInputListener(new ITextInputListener() {
                @Override
                public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
                    if (oldInput != null) {
                        oldInput.removeDocumentListener(documentListener);
                    }
                    if (newInput != null) {
                        newInput.addDocumentListener(documentListener);
                    }
                }

                @Override
                public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
                    if (oldInput != null) {
                        StyleDocument doc = (StyleDocument)oldInput;
                        doc.setViewer(null);
                    }
                    if (newInput != null) {
                        StyleDocument doc = (StyleDocument)newInput;
                        doc.setViewer(StyleViewer.this);
                    }

                    nodeVisibleChecker.clear();

                    if (!isProjectionMode()) {
                        enableProjection();
                    }
                }
            });

        configure(createSourceViewerConfiguration());

        StyledText st = getTextWidget();

        resMgr = new UIResourceManager(st);

        setPropotionalFont(getTextWidget().getFont());
        setMonospaceFont(getTextWidget().getFont());

        st.setCaret(null);
        st.setCursor(null);

        /**
           false にすると、 Hyperlink の色替えなどで StyleRange を変更した際に (resetCache() -> setScrollBars() の流れ) 横スクロールバーが
           一瞬表示されてしまう。SWTのバグでは？
           なぜかポップアップウィンドウではその現象が起きない。
           現状はtrueのままで。
         */
        //st.setAlwaysShowScrollBars(false);

        /**
         * 縦のマージンは使わない。ウィジェットの周りにレイアウト的に描画されない部分を確保するもので、
         * ドキュメントの周りに空白を追加するものではない。
         */
        //st.setTopMargin(0);
        //st.setBottomMargin(0);
        st.setLeftMargin(16);
        st.setRightMargin(16);

        st.setLineSpacing(2);

        st.setKeyBinding(SWT.ARROW_UP, SWT.NULL);
        st.setKeyBinding(SWT.ARROW_DOWN, SWT.NULL);
        st.setKeyBinding(SWT.PAGE_UP, SWT.NULL);
        st.setKeyBinding(SWT.PAGE_DOWN, SWT.NULL);
        st.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        scrollUp();
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        scrollDown();
                    } else if (event.keyCode == SWT.PAGE_UP) {
                        scrollPageUp();
                    } else if (event.keyCode == SWT.PAGE_DOWN) {
                        scrollPageDown();
                    }
                }

                @Override
                public void keyReleased(KeyEvent event) {
                }
            });

        st.addDisposeListener(event -> viewerDisposed());

        setEditable(false);

        nodeVisibleChecker = new StyleNodeVisibleEventChecker();
        st.addPaintListener(nodeVisibleChecker);

        annotationPainterManager = new AnnotationPainterManager();

        mouseManager = new StyleMouseManager(this);

        mouseManager.addListener(new CursorChanger());

        popupManager = new PopupWindowManager(this);

        funcMap = new HashMap<>();
        mouseManager.addListener(new FunctionExecutor());

        addEventFunction(EVENT_FUNC_OPEN_URL, (viewer, node, attr, event) -> openUrl(node));

        projSupport = new ProjectionSupport(this, new AnnotationAccess(), resMgr);
        projSupport.install();
    }

    protected void viewerDisposed() {
        popupManager.dispose();
        mouseManager.dispose();

        resMgr = null;
    }

    protected SourceViewerConfiguration createSourceViewerConfiguration() {
        return new StyleViewerConfiguration();
    }

    @Override
    public StyleDocument getDocument() {
        return (StyleDocument)super.getDocument();
    }

    // DO NOT use other setDocument()
    public void setDocument(StyleDocument document) {
        rootAnnotationModel = new AnnotationModel();

        super.setDocument(document, rootAnnotationModel);
    }

    @Override
    public void refresh() {
        // TextViewer.refresh() では AnnotationModel == null で呼び出しているため、 AnnotationModel が消えてしまう。
        // SourceViewer でやっておかないといけない処理だと思うのだが…
        setDocument(getDocument(), rootAnnotationModel);
    }

    public IRegion getViewportRegion() {
        // getVisibleRegion() は、見えている範囲を返すメソッドではない
        int start = getTopIndexStartOffset();
        int end = getBottomIndexEndOffset();
        return new Region(start, end - start + 1);
    }


    public AnnotationModel getTypedAnnotationModel(String type) {
        if (rootAnnotationModel == null)
            throw new IllegalStateException();

        AnnotationModel m = (AnnotationModel)rootAnnotationModel.getAnnotationModel(type);
        if (m == null) {
            m = new AnnotationModel();
            rootAnnotationModel.addAnnotationModel(type, m);
        }
        return m;
    }

    public void removeTypedAnnotationModel(String type) {
        if (rootAnnotationModel == null)
            throw new IllegalStateException();

        rootAnnotationModel.removeAnnotationModel(type);
    }

    public void removeAllAnnotations(String type) {
        // AnnotationModel.removeAllAnnotations() では、 Painter に登録された StyleRange が消えない。
        // 個別に remove すると消える。そういうものなのかバグなのか使い方の問題なのか。
        AnnotationModel am = getTypedAnnotationModel(type);
        for (Annotation a : CUtils.toList((Iterator<Annotation>)am.getAnnotationIterator())) {
            am.removeAnnotation(a);
        }
    }

    public AnnotationPainterManager getAnnotationPainterManager() {
        return annotationPainterManager;
    }

    public UIResourceManager getResourceManager() {
        return resMgr;
    }

    public PopupWindowManager getPopupWindowManager() {
        return popupManager;
    }

    public Font getPropotionalFont() {
        return propotionalFont;
    }

    public void setPropotionalFont(Font font) {
        if (font == null)
            throw new NullPointerException();

        if (!font.equals(propotionalFont)) {
            propotionalFont = font;
            getTextWidget().setFont(font);
        }
    }

    public Font getMonospaceFont() {
        return monospaceFont;
    }

    public void setMonospaceFont(Font font) {
        if (font == null)
            throw new NullPointerException();

        if (!font.equals(monospaceFont)) {
            monospaceFont = font;
        }
    }

    public StyleMouseManager getMouseManager() {
        return mouseManager;
    }

    public void addEventFunction(String name, EventFunction func) {
        funcMap.put(name, func);
    }

    public EventFunction getEventFunction(String name) {
        return funcMap.get(name);
    }

    public void runWithNoRedraw(Runnable runner) {
        // redraw(false) の状態で Document にテキストを追加したときに、選択範囲が 0-0 だと
        // 選択範囲内に追加されたと判定されて、選択範囲が 0-テキストの長さ にセットされてしまう。
        // redraw() をオフにしなければ、選択範囲は 0-0 のまま。
        Point oldSelectedRange = getSelectedRange();

        setRedraw(false);

        runner.run();

        setRedraw(true);

        Point newSelectedRange = getSelectedRange();
        if (!newSelectedRange.equals(oldSelectedRange)) {
            setSelectedRange(oldSelectedRange.x, oldSelectedRange.y);
        }
    }

    public void scrollToLine(int lineIndex) {
        StyledText st = getTextWidget();
        setTopIndex(lineIndex);
    }

    public void scrollToRelativeLine(int lineDiff) {
        StyledText st = getTextWidget();
        int lineIndex = st.getTopIndex();
        lineIndex = JUtils.clamp(lineIndex + lineDiff, 0, st.getLineCount() - 1);
        scrollToLine(lineIndex);
    }

    public void scrollUpLine() {
        scrollToRelativeLine(-1);
    }

    public void scrollDownLine() {
        scrollToRelativeLine(1);
    }

    public void scrollUp() {
        StyledText st = getTextWidget();
        st.setTopPixel(st.getTopPixel() - 16);
    }

    public void scrollDown() {
        StyledText st = getTextWidget();
        st.setTopPixel(st.getTopPixel() + 16);
    }

    public void scrollPageUp() {
        StyledText st = getTextWidget();
        Rectangle bounds = st.getBounds();
        st.setTopPixel(st.getTopPixel() - bounds.height);
    }

    public void scrollPageDown() {
        StyledText st = getTextWidget();
        Rectangle bounds = st.getBounds();
        st.setTopPixel(st.getTopPixel() + bounds.height);
    }

    public TextStyleNode getTextStyleNode(int offset) {
        return getDocument() != null ? getDocument().getTextStyleNode(offset) : null;
    }

    public TextStyleNode getTextStyleNodeForDisplay(Point displayLocation) {
        int widgetOffset = UIUtils.getOffsetForDisplayLocation(getTextWidget(), displayLocation);

        int offset = -1;
        if (this instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= this;
            offset = extension.widgetOffset2ModelOffset(widgetOffset);
        } else {
            offset = widgetOffset + getVisibleRegion().getOffset();
        }

        return getTextStyleNode(offset);
    }

    public TextStyleNode getTextStyleNodeForCursorLocation() {
        return getTextStyleNode(UIUtils.getOffsetForCursorLocation(getTextWidget()));
    }

    protected void openUrl(StyleNode node) {
        String urlStr = null;

        String href = (String)node.getCascadedAttribute(StyleAttribute.ATTRIBUTE_HREF);
        if (JUtils.isNotEmpty(href)) {
            urlStr = href;
        } else {
            String nodeText = node.buildText();
            if (JUtils.isNotEmpty(nodeText)) {
                urlStr = nodeText;
            }
        }
        if (urlStr == null)
            return;

        URL url = HttpUtils.toURL(urlStr);
        if (url == null)
            return;

        Program.launch(urlStr);
    }

    @Override
    protected StyledText createTextWidget(Composite parent, int styles) {
        /**
         * パートを切り替える際に、 StackRenderer.showTab() の中で Composite.layout(true, true) が呼び出される。
         * ここでレイアウトキャッシュを使わずに全てを再計算している。 StyledText のサイズも computeSize() が呼び出されて
         * 再計算されるが、この処理が非常に重たい。テキストの量によっては数十秒止まる。
         * その状況を回避するために、ワークアラウンドとして StyledText 内部で常にキャッシュからサイズを返すようにする。
         * 本当にキャッシュの破棄が必要な状況で問題がでるかも。
         *
         * ウィジェットサイズが変わらないのにで全て再計算するのがおかしいと思うのだが。
         * レイアウトの計算が重いパートは全て切り替えが重くなるはず。
         * 15.1.24
         *
         * 15.7.6  この処理を外して問題ないのかどうか様子見。
         *
         */
        StyledText st = new StyledText(parent, styles) {
                private Point cachedSize;
/*
                @Override
                public Point computeSize(int wHint, int hHint, boolean changed) {
                    if (cachedSize == null) {
                        cachedSize = super.computeSize(wHint, hHint, changed);
                    }
                    return cachedSize;
                }
*/
            };
        return st;
    }

    public static class StyleNodeVisibleEvent {
        List<TextStyleNode> shownNodes;
        List<TextStyleNode> hiddenNodes;

        public List<TextStyleNode> getShownNodes() {
            return shownNodes;
        }

        public List<TextStyleNode> getHiddenNodes() {
            return hiddenNodes;
        }
    }

    public static interface StyleNodeVisibleListener {
        void visibleChanged(StyleNodeVisibleEvent event);
    }

    private List<StyleNodeVisibleListener> styleNodeVisibleListeners = new ArrayList<>();

    public void addStyleNodeVisibleListener(StyleNodeVisibleListener listener) {
        if (!styleNodeVisibleListeners.contains(listener))
            styleNodeVisibleListeners.add(listener);
    }

    public void removeStyleNodeVisibleListener(StyleNodeVisibleListener listener) {
        styleNodeVisibleListeners.remove(listener);
    }

    class StyleNodeVisibleEventChecker implements PaintListener {
        List<TextStyleNode> visibleNodes;

        StyleNodeVisibleEventChecker() {
            visibleNodes = new ArrayList<>();
        }

        void clear() {
            visibleNodes.clear();
        }

        @Override
        public void paintControl(PaintEvent paintEvent) {
            List<TextStyleNode> oldVisibleNodes = visibleNodes;
            IRegion visibleRegion = getViewportRegion();
            StyleDocument doc = getDocument();
            visibleNodes = doc.getTextStyleNodes(visibleRegion.getOffset(), visibleRegion.getLength());
            List<TextStyleNode> shownNodes = visibleNodes.stream().
                filter(n -> !oldVisibleNodes.contains(n)).
                collect(Collectors.toList());
            List<TextStyleNode> hiddenNodes = oldVisibleNodes.stream().
                filter(n -> !visibleNodes.contains(n)).
                collect(Collectors.toList());

            StyleNodeVisibleEvent event = new StyleNodeVisibleEvent();
            event.shownNodes = shownNodes;
            event.hiddenNodes = hiddenNodes;

            if (shownNodes.size() > 0 || hiddenNodes.size() > 0) {
                for (StyleNodeVisibleListener l : styleNodeVisibleListeners) {
                    l.visibleChanged(event);
                }
            }
        }
    }

    class CursorChanger extends StyleMouseAdapter {
        @Override
        public void onEnter(StyleViewer viewer, StyleNode node, MouseEvent event) {
            StyledText st = viewer.getTextWidget();
            if (UIUtils.isDisposed(st))
                return;

            int cursorType = -1;
            String value = (String)node.getCascadedAttribute(StyleAttribute.ATTRIBUTE_CURSOR);
            if (value != null) {
                switch (value) {
                case "arrow":
                    cursorType = SWT.CURSOR_ARROW;
                    break;
                case "wait":
                    cursorType = SWT.CURSOR_WAIT;
                    break;
                case "crosshair":
                    cursorType = SWT.CURSOR_CROSS;
                    break;
                case "help":
                    cursorType = SWT.CURSOR_HELP;
                    break;
                case "pointer":
                    cursorType = SWT.CURSOR_HAND;
                    break;
                case "no":
                    cursorType = SWT.CURSOR_NO;
                    break;
                case "text":
                    cursorType = SWT.CURSOR_IBEAM;
                    break;
                }
            }
            if (cursorType >= 0) {
                st.setCursor(UIUtils.getSystemCursor(cursorType));
            }
        }

        @Override
        public void onExit(StyleViewer viewer, StyleNode node) {
            StyledText st = viewer.getTextWidget();
            if (UIUtils.isDisposed(st))
                return;

            st.setCursor(null);
        }
    }

    class FunctionExecutor extends StyleMouseAdapter {
        @Override
        public void onEnter(StyleViewer viewer, StyleNode node, MouseEvent event) {
            executeFunc(viewer, node, StyleAttribute.ATTRIBUTE_ONENTER, event);
        }

        @Override
        public void onExit(StyleViewer viewer, StyleNode node) {
            executeFunc(viewer, node, StyleAttribute.ATTRIBUTE_ONEXIT, null);
        }

        @Override
        public void onMove(StyleViewer viewer, StyleNode node, MouseEvent event) {
            executeFunc(viewer, node, StyleAttribute.ATTRIBUTE_ONMOVE, event);
        }

        @Override
        public void onClick(StyleViewer viewer, StyleNode node, MouseEvent event) {
            executeFunc(viewer, node, StyleAttribute.ATTRIBUTE_ONCLICK, event);
        }

        @Override
        public void onHover(StyleViewer viewer, StyleNode node, MouseEvent event) {
            executeFunc(viewer, node, StyleAttribute.ATTRIBUTE_ONHOVER, event);
        }

        private void executeFunc(StyleViewer viewer, StyleNode node, StyleAttribute attr, MouseEvent event) {
            while (node != null) {
                String name = node.getAttribute(attr);
                if (JUtils.isNotEmpty(name)) {
                    EventFunction func = funcMap.get(name);
                    if (func == null) {
                        throw new IllegalStateException(String.format("Event function [%s] is not defined.", name));
                    }
                    if (func != null) {
                        func.execute(viewer, node, attr, event);
                    }
                }

                node = node.getParent();
            }
        }
    }

    public class AnnotationPainterManager {
        StyleViewer viewer;
        AnnotationPainter painter;

        AnnotationPainterManager() {
            viewer = StyleViewer.this;

            painter = new AnnotationPainter(viewer, new AnnotationAccess());
            viewer.addTextPresentationListener(painter);
            viewer.addPainter(painter);
        }

        public AnnotationPainter getPainter() {
            return painter;
        }

        public void addType(String type) {
            painter.addAnnotationType(type, type);

            if (viewer.overviewRuler != null) {
                viewer.overviewRuler.addAnnotationType(type);
            }
        }

        public void removeType(String type) {
            painter.removeAnnotationType(type);

            if (viewer.overviewRuler != null) {
                viewer.overviewRuler.removeAnnotationType(type);
            }
        }

        public void setPresentation(
            String type,
            AnnotationPainter.ITextStyleStrategy textStrategy,
            RGB highlightColor,
            boolean showOverviewRuler,
            RGB overviewColor) {
            painter.addTextStyleStrategy(type, textStrategy);
            painter.setAnnotationTypeColor(type, getResourceManager().getColor(highlightColor));

            if (viewer.overviewRuler != null) {
                viewer.overviewRuler.setAnnotationTypeLayer(type, 0);
                viewer.overviewRuler.setAnnotationTypeColor(type, getResourceManager().getColor(overviewColor));
            }
        }
    }

    static class InternalOverviewRuler extends ModifiedOverviewRuler {
        InternalOverviewRuler() {
            super(new AnnotationAccess(), 16, new UIResourceManager());
        }
    }
}


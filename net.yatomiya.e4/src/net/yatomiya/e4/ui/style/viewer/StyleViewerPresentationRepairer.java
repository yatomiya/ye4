/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import java.lang.reflect.*;
import java.util.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.util.*;


class StyleViewerPresentationRepairer implements IPresentationDamager, IPresentationRepairer {
    private StyleViewer viewer;
    private StyleDocument document;

    StyleViewerPresentationRepairer(StyleViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void setDocument(IDocument document) {
        this.document = (StyleDocument)document;
    }

    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent event, boolean documentPartitioningChanged) {
        StyleDocument document = (StyleDocument)event.getDocument();
        int replacedOffset = event.getOffset();
        int replacedLength = event.getLength();
        String insertedText = event.getText();
        if (insertedText.length() == 0)
            return new Region(replacedOffset, 0);

        List<StyleNode> list = document.getStyleNodes(replacedOffset, insertedText.length());
        if (list.size() == 0)
            return new Region(replacedOffset, 0);

        int offset = 0;
        int length = 0;
        offset = list.get(0).getOffset();
        length = list.get(list.size() - 1).getEnd() - offset + 1;
        IRegion region = new Region(offset, length);

        /**
         * StyledTextはライン毎にそのラインの属性(indent,bgcolorなど)を保持し、内部テキストが変更されると
         * 対応する位置のライン属性の位置も自動的に更新する。
         * 挿入テキストに改行が含まれていた場合、ライン数が増えてライン属性が新規に作成されるが、属性の値は初期値となる。
         * 挿入テキストのRegionだけでダメージ範囲を計算すると、その新規ラインの範囲がダメージ範囲に含まれない。
         * そうなると、createPresentation()で新規ラインが範囲外になってしまうため、新規ラインの属性が適用されない。
         * そこで、新規ラインの属性を適用させるために、挿入テキストの最後が改行である場合、ダメージ範囲に+1して新規ライン先頭の
         * 文字もダメージ範囲に含めるようにする。
         */
        if (insertedText.charAt(insertedText.length() - 1) == '\n'
            && (offset + length) < document.getLength() - 1) {
            region = new Region(offset, length + 1);
        }

        return region;
    }

    @Override
    public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
        if (viewer == null || document == null || document.getRootStyleNode() == null)
            return;

        int offset = damage.getOffset();
        int length = damage.getLength();

        /**
         * !setDefaultStyleRange()は使わないように。
         * 再描画するさいに、TextViewer.addPresentation() の中で default StyleRange のあるなしで分岐するが、
         * default StyleRange が存在すると StyledText.replaceStyleRange(0,0,...) が呼ばれる。この引数だと、
         * 内部で最終的に全テキスト範囲のresetCache()が呼ばれてしまい、キャッシュの再計算処理が非常に重たい。なので使わないように。
         */
        presentation.mergeStyleRange(new StyleRange(offset, length, null, null));

        List<StyleRange> list = new ArrayList<>();

        for (StyleNode node : document.getStyleNodes(offset, length))  {
            StyleRange range = createStyleRange(node);
            if (range != null)
                list.add(range);
        }

        presentation.mergeStyleRanges(list.toArray(new StyleRange[list.size()]));

        setLineAccessor.setLineIndent(viewer, offset, length);
    }

    private StyleRange createStyleRange(StyleNode node) {
        String text = node.getText();
        Map<StyleAttribute, Object> map = node.getContextAttributeMap();
        if (JUtils.isEmpty(text) || map.size() == 0)
            return null;

        StyleRange range = new StyleRange(node.getOffset(), node.getLength(), null, null);
        boolean isSet = false;
        for (Map.Entry<StyleAttribute, Object> entry : map.entrySet()) {
            isSet |= entry.getKey().applyStyleRange(range, entry.getValue(), node, viewer);
        }

        return isSet ? range : null;
    }

    static SetLineIndentAccessor setLineAccessor = new SetLineIndentAccessor();

    /**
     * StyledText.setLineIndent() の中で resetCache(),redrawLines() が呼ばれる。 setLineIndent() はひとつの区域の指定しかできず、
     * メッセージ内で細かく区分けしたラインブロックのインデントの設定をメッセージの数だけ呼び出さなければならない。
     * setLineIndent() を呼び出す毎に内部のキャッシュ再計算が行われ、実感できるほどの処理落ちを引き起こしてしまう。
     * 解決策として、 setLineIndent() をばらして全てのパラメータのセットの後に、一度だけ resetCache() を呼び出すようにする。
     */
    static class SetLineIndentAccessor {
        Field rendererField;
        Method setLineIndentMethod;
        Method setLineWrapIndentMethod;
        Method setLineAlignmentMethod;
        Method setLineBackgroundMethod;
        Method resetCacheMethod;
        Method redrawLinesMethod;

        SetLineIndentAccessor() {
            try {
                Class clz = StyledText.class;
                rendererField = clz.getDeclaredField("renderer");
                rendererField.setAccessible(true);
                Class rendererClz = rendererField.getType();
                setLineIndentMethod = rendererClz.getDeclaredMethod("setLineIndent", int.class, int.class, int.class);
                setLineIndentMethod.setAccessible(true);
                setLineWrapIndentMethod = rendererClz.getDeclaredMethod("setLineWrapIndent", int.class, int.class, int.class);
                setLineWrapIndentMethod.setAccessible(true);
                setLineAlignmentMethod = rendererClz.getDeclaredMethod("setLineAlignment", int.class, int.class, int.class);
                setLineAlignmentMethod.setAccessible(true);
                setLineBackgroundMethod = rendererClz.getDeclaredMethod("setLineBackground", int.class, int.class, Color.class);
                setLineBackgroundMethod.setAccessible(true);

                resetCacheMethod = StyledText.class.getDeclaredMethod("resetCache", int.class, int.class);
                resetCacheMethod.setAccessible(true);
                redrawLinesMethod = StyledText.class.getDeclaredMethod("redrawLines", int.class, int.class, boolean.class);
                redrawLinesMethod.setAccessible(true);
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        void setLineIndent(StyleViewer viewer, int offset, int length) {
            try {
                StyleDocument doc = viewer.getDocument();
                StyledText st = viewer.getTextWidget();
                Object renderer = rendererField.get(st);
                List<StyleNode> list = doc.getStyleNodes(offset, length);
                if (list.size() == 0)
                    return;
                int lineProcessed = -1;
                int lineBlockStart = -1;
                int lineBlockEnd = -1;
                for (StyleNode node : list) {
                    int modelStartLine = doc.getLineOfOffset(node.getOffset());
                    int si = viewer.modelLine2WidgetLine(modelStartLine);
                    if (lineBlockStart < 0)
                        lineBlockStart = si;
                    int endOffset = node.getOffset();
                    if (node.getLength() > 0)
                        endOffset += node.getLength() - 1;
                    int modelEndLine = doc.getLineOfOffset(endOffset);
                    int ei = -1;
                    while (true) {
                        ei = viewer.modelLine2WidgetLine(modelEndLine);
                        if (ei >= 0)
                            break;
                        modelEndLine--;
                        if (modelEndLine <= modelStartLine)
                            break;
                    }
                    if (ei < 0)
                        continue;
                    if (lineBlockEnd < ei)
                        lineBlockEnd = ei;

                    if (lineProcessed >= ei)
                        continue;
                    lineProcessed = ei;
                    int lineCount = ei - si + 1;

                    LineAttribute attr = createLineAttribute(viewer, node);
                    if (attr != null) {
                        setLineBackgroundMethod.invoke(renderer, si, lineCount, attr.bgColor);
                        setLineAlignmentMethod.invoke(renderer, si, lineCount, attr.alignment);
                        setLineIndentMethod.invoke(renderer, si, lineCount, attr.indent);
                        setLineWrapIndentMethod.invoke(renderer, si, lineCount, attr.indent);
                    }
                }

                int lineCount = lineBlockEnd - lineBlockStart + 1;
                if (lineCount == 0)
                    lineCount = 1;
                resetCacheMethod.invoke(st, lineBlockStart, lineCount);
                redrawLinesMethod.invoke(st, lineBlockStart, lineCount, true);
            } catch (BadLocationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        private LineAttribute createLineAttribute(StyleViewer viewer, StyleNode node) {
            String text = node.getText();
            Map<StyleAttribute, Object> map = node.getContextAttributeMap();
            if (JUtils.isEmpty(text) || map.size() == 0)
                return null;

            LineAttribute line = new LineAttribute();
            boolean isSet = false;
            for (Map.Entry<StyleAttribute, Object> entry : map.entrySet()) {
                StyleAttribute attr = entry.getKey();
                Object value = entry.getValue();
                isSet |= attr.applyLineAttribute(line, value, node, viewer);
            }

            return isSet ? line : null;
        }
    }


}



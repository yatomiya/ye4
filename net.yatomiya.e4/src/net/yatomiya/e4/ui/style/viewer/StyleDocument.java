/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.style.viewer;

import java.util.*;
import org.eclipse.jface.text.*;
import net.yatomiya.e4.ui.style.*;
import net.yatomiya.e4.util.*;

public class StyleDocument extends Document implements IDocumentListener {
    private StyleNode rootNode;
    private StyleViewer viewer;

    private List<StyleNode> textNodeList;

    public StyleDocument() {
        super();

        textNodeList = new ArrayList<>();

        addDocumentListener(this);

        rootNode = StyleTag.SPAN.createStyleNode();
        rootNode.setData(StyleDocument.class, this);
    }

    void setViewer(StyleViewer viewer) {
        StyleViewer oldViewer = this.viewer;

        this.viewer = viewer;
        getRootStyleNode().setData(StyleViewer.class, viewer);
    }

    public StyleViewer getViewer() {
        return viewer;
    }

    public StyleNode getRootStyleNode() {
        return rootNode;
    }

    @Override
    public String get(int offset, int length) {
        try {
            return super.get(offset, length);
        } catch (BadLocationException e) {
            return null;
        }
    }

    @Override
    public void set(String text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(String text, long modificationStamp) {
        throw new UnsupportedOperationException();
    }

    protected void nodeChanged() {
        textNodeList.clear();
        if (rootNode != null) {
            rootNode.visitNodeTree(node -> {
                    if (!JUtils.isEmpty(node.getText())) {
                        textNodeList.add(node);
                    }
                    return true;
                });
        }
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
        nodeChanged();
    }

    @Override
    public void documentChanged(DocumentEvent event) {
    }

    public StyleNode getStyleNode(int offset) {
        return RegionUtils.find(textNodeList, offset);
    }

    public List<StyleNode> getStyleNodes(int offset, int length) {
        return RegionUtils.find(textNodeList, offset, length);
    }

}

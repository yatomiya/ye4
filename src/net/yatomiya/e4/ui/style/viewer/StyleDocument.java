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
import net.yatomiya.e4.ui.viewer.*;
import net.yatomiya.e4.util.*;

public class StyleDocument extends Document implements IDocumentListener {
    private StyleNode rootNode;
    private StyleViewer viewer;

    private List<TextStyleNode> textNodeList;

    public StyleDocument() {
        super();

        textNodeList = new ArrayList<>();

        addDocumentListener(this);

        rootNode = StyleTag.TAG_SPAN.createStyleNode();
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

    protected void nodeChanged() {
        textNodeList.clear();
        if (rootNode != null) {
            rootNode.visitNodeTree(node -> {
                    if (node instanceof TextStyleNode) {
                        TextStyleNode tnode = (TextStyleNode)node;
                        if (JUtils.isNotEmpty(tnode.getText())) {
                            textNodeList.add(tnode);
                        }
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

    public TextStyleNode getTextStyleNode(int offset) {
        return JFaceTextUtils.find(textNodeList, offset);
    }

    public List<TextStyleNode> getTextStyleNodes(int offset, int length) {
        return JFaceTextUtils.find(textNodeList, offset, length);
    }

}

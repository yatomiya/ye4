/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.dialogs;

import java.util.*;
import java.util.List;
import org.eclipse.e4.ui.model.application.descriptor.basic.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.util.*;
import net.yatomiya.e4.ui.viewer.*;
import net.yatomiya.e4.util.*;

public class ShowViewDialog extends TableViewerDialog {
    public static final String SHOW_IN_SHOW_VIEW_DIALOG = "SHOW_IN_SHOW_VIEW_DIALOG";

    protected Button okButton;
    protected UIResourceManager resMgr;

    public ShowViewDialog(Shell parentShell) {
        super(parentShell, SWT.SINGLE, "ビューを選択してください");

        resMgr = new UIResourceManager();
    }

    @Override
    protected void setupViewer(TableViewer viewer) {
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);

        viewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((MPartDescriptor)element).getLocalizedLabel();
                }

                @Override
                public Image getImage(Object element) {
                    String uri = ((MPartDescriptor)element).getIconURI();
                    return resMgr.getImage(uri);
                }
            });

        viewer.setContentProvider(ArrayContentProvider.getInstance());

        viewer.addPostSelectionChangedListener(event -> {
                okButton.setEnabled(!SelectionUtils.isEmpty(event.getSelection()));
            });

        viewer.addOpenListener(event -> {
                if (SelectionUtils.size(event.getSelection()) > 0) {
                    okButton.notifyListeners(SWT.Selection, new Event());
                }
            });

        List<MPartDescriptor> list = new ArrayList<>();
        for (MPartDescriptor d : EModelUtils.getApplication().getDescriptors()) {
            if (d.getTags().contains(SHOW_IN_SHOW_VIEW_DIALOG))
                list.add(d);
        }
        viewer.setInput(list.toArray());
    }

    protected ILabelProvider createLabelProvider() {
        return new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((MPartDescriptor)element).getLocalizedLabel();
            }

            @Override
            public Image getImage(Object element) {
                String uri = ((MPartDescriptor)element).getIconURI();
                return resMgr.getImage(uri);
            }
        };
    }

    protected IContentProvider createContentProvider() {
        return ArrayContentProvider.getInstance();
    }
}

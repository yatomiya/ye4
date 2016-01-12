/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.image;

import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.services.image.*;

public class ImageViewerWindow extends Window {
    protected ScrolledComposite basePane;
    protected ImageCanvas canvas;
    protected String url;
    protected ImageType imageType;

    public ImageViewerWindow(Shell parentShell, String url, ImageType imageType) {
        super(parentShell);

        this.url = url;
        this.imageType = imageType;

        setShellStyle(SWT.MODELESS | SWT.SHELL_TRIM);
    }

    @Override
    protected void configureShell(Shell shell) {
        shell.setText(url);
    }

    @Override
    protected Control createContents(Composite parent) {
        parent.setLayout(new FillLayout());

        basePane = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        basePane.addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    if (canvas.isFitToSize()) {
                        canvas.setSize(basePane.getSize());
                    }
                }
            });

        canvas = new ImageCanvas(basePane, SWT.NONE, url, imageType) {
                @Override
                protected void handleImageEvent(ImageEvent event) {
                    super.handleImageEvent(event);
                }
            };
        canvas.setSize(256, 256);
        basePane.setContent(canvas);
        configureCanvas(canvas);

        return basePane;
    }

    protected void configureCanvas(ImageCanvas canvas) {
        canvas.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent event) {
                    canvas.setFitToSize(!canvas.isFitToSize());

                    if (canvas.isFitToSize()) {
                        canvas.setSize(basePane.getSize());
                    } else {
                        canvas.setSize(canvas.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                    }
                }
            });
    }
}



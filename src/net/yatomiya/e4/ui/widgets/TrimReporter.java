/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.widgets;

/*
public class TrimReporter {
    private Composite parentComposite;
    private Composite basePane;
    private Label messageLabel;

    @PostConstruct
    public void postConstruct(Composite parentComposite) {
        this.parentComposite = parentComposite;

        basePane = new Composite(parentComposite, SWT.NONE) {
                public Point computeSize(int wHint, int hHint, boolean changed) {
                    Point p = super.computeSize(wHint, hHint, changed);
                    if (p.x < 640)
                        p.x = 640;
                    return p;
                }
            };
        basePane.setLayout(new FillLayout());

        messageLabel = new Label(basePane, SWT.LEFT);

        Redegg.getUIService().getStatusLineManager().subscribe(
            this,
            msg -> {
                showMessage(msg);
            });
    }

    @PreDestroy
    public void preDestroy() {
        Redegg.getUIService().getStatusLineManager().unsubscribe(this);
    }

    void showMessage(String text) {
        messageLabel.setText(text);
    }
}
*/

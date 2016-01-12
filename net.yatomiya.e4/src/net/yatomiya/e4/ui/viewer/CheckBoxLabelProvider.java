/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;


public abstract class CheckBoxLabelProvider extends OwnerDrawLabelProvider {
    private static Image checkedImage;
    private static Image uncheckedImage;

    public CheckBoxLabelProvider(ColumnViewer viewer) {
        if (checkedImage == null) {
            checkedImage = makeShot(viewer.getControl(), true);
            uncheckedImage = makeShot(viewer.getControl(), false);
        }
    }

    private static Image makeShot(Control control, boolean type) {
        // Hopefully no platform uses exactly this color because we'll make
        // it transparent in the image.
//        Color greenScreen = new Color(control.getDisplay(), 222, 223, 224);
        Color greenScreen = new Color(control.getDisplay(), 222, 0, 0);

        Shell shell = new Shell(control.getShell(), SWT.NO_TRIM);

        // otherwise we have a default gray color
        shell.setBackground(greenScreen);

        if (Util.isMac()) {
            Button button2 = new Button(shell, SWT.CHECK);
            Point bsize = button2.computeSize(SWT.DEFAULT, SWT.DEFAULT);

            // otherwise an image is stretched by width
            bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
            bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
            button2.setSize(bsize);
            button2.setLocation(100, 100);
        }

        Button button = new Button(shell, SWT.CHECK);
        button.setBackground(greenScreen);
        button.setSelection(type);

        // otherwise an image is located in a corner
        button.setLocation(1, 1);
        Point bsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // otherwise an image is stretched by width
        bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
        bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
        button.setSize(bsize);

        shell.setSize(bsize);

        shell.open();

        GC gc = new GC(shell);
        Image image = new Image(control.getDisplay(), bsize.x, bsize.y);
        gc.copyArea(image, 0, 0);
        gc.dispose();
        shell.close();

        ImageData imageData = image.getImageData();
        imageData.transparentPixel = imageData.palette.getPixel(greenScreen.getRGB());

        Image img = new Image(control.getDisplay(), imageData);
        image.dispose();

        return img;
    }

    public Image getImage(Object element) {
        if (isChecked(element)) {
            return checkedImage;
        } else {
            return uncheckedImage;
        }
    }

    @Override
    protected void measure(Event event, Object element) {
        event.height = getImage(element).getBounds().height;
    }

    @Override
    protected void erase(Event event, Object element) {
        // OwnerDrawLabelProvider.erase で背景を SWT.COLOR_LIST_SELECTION で塗りつぶしてるが
        // 他のセルの選択色とは違う色となっている。
        // 背景色を塗らないことで対処。
    }

    @Override
    protected void paint(Event event, Object element) {

        Image img = getImage(element);

        if (img != null) {
            Rectangle bounds;

            if (event.item instanceof TableItem) {
                bounds = ((TableItem) event.item).getBounds(event.index);
            } else {
                bounds = ((TreeItem) event.item).getBounds(event.index);
            }

            Rectangle imgBounds = img.getBounds();
            bounds.width /= 2;
            bounds.width -= imgBounds.width / 2;
            bounds.height /= 2;
            bounds.height -= imgBounds.height / 2;

            int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
            int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

            if (SWT.getPlatform().equals("carbon")) {
                event.gc.drawImage(img, x + 2, y - 1);
            } else {
                event.gc.drawImage(img, x, y - 1);
            }

        }
    }

    protected abstract boolean isChecked(Object element);
}

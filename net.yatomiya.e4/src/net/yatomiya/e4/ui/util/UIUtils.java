/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import org.eclipse.e4.core.contexts.*;
import org.eclipse.e4.ui.model.application.ui.basic.*;
import org.eclipse.e4.ui.model.application.ui.menu.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import net.yatomiya.e4.ui.workbench.renderers.swt.*;
import net.yatomiya.e4.util.*;

public class UIUtils {
    public static boolean isDisposed(Control c) {
        return c == null || c.isDisposed();
    }

    public static boolean isDisposed(Viewer viewer) {
        return viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed();
    }

    public static String buildModifiedText(String text, int start, int end, String insertText) {
        if (start < text.length()) {
            String v0 = text.substring(0, start);
            String v1;
            if (end < text.length() - 1) {
                v1 = text.substring(end, text.length());
            } else {
                v1 = "";
            }
            return v0 + insertText + v1;
        } else {
            return text + insertText;
        }

    }

    public static void recursiveSetEnabled(Composite composite, boolean enabled) {
        composite.setEnabled(enabled);
        for (Control c : composite.getChildren()) {
            if (c instanceof Composite) {
                recursiveSetEnabled((Composite)c, enabled);
            } else {
                c.setEnabled(enabled);
            }
        }
    }

    public static boolean isAncesterOf(Composite ancestor, Control descendant) {
        while (descendant != null) {
            if (ancestor == descendant)
                return true;
            descendant = descendant.getParent();
        }
        return false;
    }

    public static Rectangle getDisplayBounds(Control control) {
        Rectangle bounds = control.getBounds();
        Point loc = control.toDisplay(bounds.x, bounds.y);
        bounds.x = loc.x;
        bounds.y = loc.y;
        return bounds;
    }

    public static Monitor getMonitorAt(int x, int y) {
        for (Monitor m : Display.getDefault().getMonitors()) {
            Rectangle r = m.getBounds();
            if (r.contains(x, y))
                return m;
        }
        return null;
    }

    public static RGB toRGB(String value) {
        String[] strRgb = StringUtils.split(value, ",");
        try {
            int r = Integer.valueOf(strRgb[0]);
            int g = Integer.valueOf(strRgb[1]);
            int b = Integer.valueOf(strRgb[2]);
            return new RGB(r, g, b);
        } catch (NumberFormatException e) {
            return new RGB(0, 0, 0);
        }
    }

    public static Rectangle createRectangle(Rectangle rect) {
        return new Rectangle(rect.x, rect.y, rect.width, rect.height);
    }

    public static Rectangle normalizeLength(Rectangle rect) {
        int x = Math.min(rect.x, rect.x + rect.width);
        int y = Math.min(rect.y, rect.y + rect.height);
        int width = rect.width >= 0 ? rect.width : -rect.width;
        int height = rect.height >= 0 ? rect.height : -rect.height;
        return new Rectangle(x, y, width, height);
    }

    public static boolean contains(Rectangle rect, Rectangle container) {
        Rectangle r0 = normalizeLength(rect);
        Rectangle r1 = normalizeLength(container);
        return ((r1.x <= r0.x) && (r0.x + r0.width) <= (r1.x + r1.width)
                && (r1.y <= r0.y) && (r0.y + r0.height) <= (r1.y + r1.height));
    }

    public static boolean intersects(Rectangle rect0, Rectangle rect1) {
        Rectangle r0 = normalizeLength(rect0);
        Rectangle r1 = normalizeLength(rect1);
        return (r0.x < r1.x + r1.width) && (r1.x < r0.x + r0.width)
            && (r0.y < r1.y + r1.height) && (r1.y < r0.y + r0.height);
    }

    public static Rectangle intersection(Rectangle rect0, Rectangle rect1) {
        if (intersects(rect0, rect1)) {
            Rectangle r0 = normalizeLength(rect0);
            Rectangle r1 = normalizeLength(rect1);
            int sx = Math.max(r0.x, r1.y);
            int ex = Math.min(r0.x + r0.width, r1.x + r1.width);
            int sy = Math.max(r0.y, r1.y);
            int ey = Math.min(r0.y + r0.height, r1.y + r1.height);
            return new Rectangle(sx, sy, ex - sx, ey - sy);
        } else {
            return null;
        }
    }

    public static void copyToClipboard(String text) {
        Clipboard c = new Clipboard(Display.getDefault());
        c.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
    }

    public static String getTextFromClipboard() {
        Clipboard c = new Clipboard(Display.getDefault());
        String data = (String)c.getContents(TextTransfer.getInstance());
        return data;
    }

    public static Point calcPopupMenuLocation(Control parent) {
        Rectangle rect = parent.getBounds();
        Point loc = new Point(rect.x - 1, rect.y + rect.height);
        return parent.getShell().getDisplay().map(parent.getParent(), null, loc);
    }

    public static Combo createDropDownCombo(Composite parent, List<String> items) {
        return createDropDownCombo(parent, items, null);
    }

    public static Combo createDropDownCombo(Composite parent, List<String> items, Consumer<Integer> handler) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        if (handler != null) {
            combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        handler.accept(combo.getSelectionIndex());
                    }
                });
        }
        for (String s : items) {
            combo.add(s);
        }
        return combo;
    }

    public static MenuItem createCascadeMenu(Menu parentMenu, String title, List<String> items, int itemStyle, Consumer<Integer> handler) {
        MenuItem casItem = new MenuItem(parentMenu, SWT.CASCADE);
        Menu cmenu = new Menu(casItem);
        casItem.setMenu(cmenu);
        casItem.setText(title);
        for (String s : items) {
            MenuItem item = new MenuItem(cmenu, itemStyle);
            item.setText(s);
            if (handler != null) {
                item.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent event) {
                            handler.accept(cmenu.indexOf(item));
                        }
                    });

            }
        }
        return casItem;
    }

    static Map<Integer,Cursor> cursorMap = new HashMap<>();

    public static Cursor getSystemCursor(int swtCursor) {
        Cursor c = cursorMap.get(swtCursor);
        if (c == null) {
            c = new Cursor(Display.getDefault(), swtCursor);
            cursorMap.put(swtCursor, c);
        }
        return c;
    }

    /**
     * 指定したロケーションの位置に存在するキャラクターのオフセットを返す。
     * StyledWidget.getOffsetAtLocation() はロケーションの位置のキャラクターのオフセットではなく、
     * その位置をクリックしたときにカレットが動く場所のオフセットを返す。そのキャラクターの水平方向に後ろ半分
     * の領域をクリックすると、次のキャラクターの位置を返す。
     */
    private static Method getOffsetAtPointMethod;

    public static int getOffsetAtLocation(StyledText st, Point point) {
        if (getOffsetAtPointMethod == null) {
            try {
                getOffsetAtPointMethod = StyledText.class.getDeclaredMethod("getOffsetAtPoint", int.class, int.class, int[].class, boolean.class);
                getOffsetAtPointMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        int[] trailing = new int[1];
        int offset = -1;
        try {
            offset = (Integer)getOffsetAtPointMethod.invoke(st, point.x, point.y, trailing, true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        if (offset == -1) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }

        // 元ソースはここでカレットの位置情報を足している
        //return offset + trailing[0];
        return offset;
    }

	public static int getOffsetForDisplayLocation(StyledText st, Point displayLocation) {
		try {
			if (isDisposed(st))
				return -1;

			Display display = st.getDisplay();
			Point relativeLocation= st.toControl(displayLocation);

			int widgetOffset= getOffsetAtLocation(st, relativeLocation);
			Point p = st.getLocationAtOffset(widgetOffset);
			if (p.x > relativeLocation.x)
				widgetOffset--;

            return widgetOffset;
		} catch (IllegalArgumentException e) {
			return -1;
		}
	}

    public static int getOffsetForCursorLocation(StyledText st) {
        if (isDisposed(st))
            return -1;

        return getOffsetForDisplayLocation(st, st.getDisplay().getCursorLocation());
    }

    static Field rendererField;
    static Field idleRunningField;

    public static boolean isLineMetricsCalculating(StyledText st) {
        try {
            if (rendererField == null) {
                rendererField = JUtils.findField(st.getClass(), "renderer");
                rendererField.setAccessible(true);
                idleRunningField = JUtils.findField(rendererField.getType(), "idleRunning");
                idleRunningField.setAccessible(true);
            }

            Object renderer = rendererField.get(st);
            Object value = idleRunningField.get(renderer);
            return (Boolean)value;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void doWhenLineMetricsCalculatingFinished(StyledText st, boolean disableWhileWaiting, Runnable runner) {
        boolean isEnabled = st.getEnabled();
        if (isEnabled && disableWhileWaiting)
            st.setEnabled(false);
        new RepeatableRunner(1, 100) {
            @Override
            public void run() {
                if (UIUtils.isDisposed(st)) {
                    cancel();
                    return;
                }

                if (!isLineMetricsCalculating(st)) {
                    runner.run();
                    cancel();

                    if (isEnabled && disableWhileWaiting)
                        st.setEnabled(true);
                }
            }
        }.start();
    }

    public static void disableUntilLineMetricsCalculatingFinished(StyledText st) {
        doWhenLineMetricsCalculatingFinished(st, true, () -> {});
    }

    /**
     * EMenuService.registerContextMenu() の内部処理を直接呼び出す。
     * EMenuService.registerContextMenu() は MPart ひとつにつきひとつのコンテキストメニューを登録する
     * 使い方を想定している。ひとつのパート内で複数のコントロールに複数のコンテキストメニューを登録したい場合には使えない。
     * この内部メソッドを使えば、コントロール毎にコンテキストメニューを登録できる。
     * ここで登録する MPopupMenu はアプリケーションモデルにつながっていなくても動作しているようだが、本当に安全かは不明。
     *
     */
    public static Menu registerContextMenu(Control control, MPopupMenu popupMenu, IEclipseContext context) {
        Method method = JUtils.findMethod(org.eclipse.e4.ui.internal.workbench.swt.MenuService.class,
                                          "registerMenu",
                                          Control.class,
                                          MPopupMenu.class,
                                          IEclipseContext.class);
        Menu menu = null;
        try {
            menu = (Menu)method.invoke(null, control, popupMenu, context);
        } catch (IllegalAccessException | InvocationTargetException e) {
        }

        if (menu == null)
            return null;

        /**
         * - registerContextMenu()を使うと、登録した PopupMenuImpl への参照が切れずにメモリに残ってしまう。
         * - Application.e4xmi に登録済みの MPopupMenu を使っても発生するので renderer 内部の問題なのでは？
         * - 参照ルートが複雑すぎて追跡を断念。
         * - 大量のメモリリークが発生するのは、 PopupMenuImpl.transientData に renderer が Control を
         * 登録し、 Control からメニューモデル、ビューオブジェクトが参照されデータが丸々メモリに残ってしまうため。
         * とりあえず、 transientData をクリアすれば メニューモデル への参照は切れて大量リークは防げる。
         * - Part.getMenu は　renderer 内部からも状態を監視しており、外部でいじると renderer 内部の状態との整合性がとれなくなる
         * 可能性がある。 Part.getMenu を使わずにモデルメニューを生成する方法があればいいのだが。
         * 15.11.23
         */
        control.addDisposeListener(
            event -> {
                // renderer の作業オブジェクトが残っているため、まとめてクリア
                popupMenu.getTransientData().clear();

                popupMenu.setToBeRendered(false);

                // 通常、 Part.getMenus() は Part のクローズ時にまとめて廃棄される。 MenuManagerRenderer 内では、
                // このタイミング以外で getMenus() 内部がいじられる状況に対応していない。
                // remove しただけでは MenuManagerRenderer 内部に PopupMenuImpl への参照が残ってしまうので、手作業でクリアする。
                try {
                    MenuManagerRenderer renderer = (MenuManagerRenderer)popupMenu.getRenderer();
                    JUtils.findMethod(MenuManagerRenderer.class, "unlinkMenu", MMenu.class).invoke(renderer, popupMenu);
                } catch (IllegalAccessException | InvocationTargetException e) {
                }
            });

        return menu;
    }

    public static Menu registerContextMenu(MPart part, String menuId, Control control) {
        MPopupMenu popupMenu = EModelUtils.find(part, MPopupMenu.class, menuId);
        if (popupMenu == null)
            return null;
        return registerContextMenu(control, popupMenu, part.getContext());
    }

    /**
     * Workaround for OLE focus bug.
     * Sometimes OLE changes focus of control internally and SWT event can not catch such changes.
     * This hacks corrects bad condition which focus of control is not in active part.
     */
/*
    public static void validatePartFocus(IEclipseContext context) {
        Control focused = Display.getDefault().getFocusControl();
        if (focused == null)
            return;

        MUIElement element = getMUIElementFor(focused);
        if (element == null)
            return;

        NPartService partSrv = NUtils.getService(NPartService.class);
        MPart part = partSrv.getPartFor(element);
        MPart activePart = partSrv.getActivePart();
        if (part != activePart) {
            Object view = activePart.getObject();
            ContextInjectionFactory.invoke(view, Focus.class, context);
        }
    }
*/
}


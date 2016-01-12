/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.ui.viewer;

public class JFaceTextUtils {

/*
    public static List<TokenRegion> fillTokenGap(List<TokenRegion> list, int startOffset, int length, TokenType tokenType) {
        List<TokenRegion> filledList = new ArrayList<>(list.size() * 2);
        int offset = startOffset;
        for (TokenRegion r : list) {
            if (offset < r.getOffset()) {
                filledList.add(new TokenRegion(offset, r.getOffset() - offset, tokenType));
            }
            filledList.add(r);
            offset = r.getOffset() + r.getLength();
        }
        if (offset < length) {
            filledList.add(new TokenRegion(offset, length - offset, tokenType));
        }
        return filledList;
    }
*/
}


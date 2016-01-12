/*******************************************************************************
 * Copyright (c) 2014,2015 Hideki Yatomi
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.yatomiya.e4.util;

import java.io.*;
import java.net.*;
import java.util.*;
import com.squareup.okhttp.*;
import com.squareup.okhttp.internal.http.*;

public class HttpUtils {
    public static String buildFormPostData(Map<String, String> valueMap, String charset) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String key : valueMap.keySet()) {
            String value = valueMap.get(key);
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(key);
            sb.append("=");
            sb.append(HttpUtils.urlEncode(value, charset));
        }
        return sb.toString();
    }

    public static URL toURL(String str) {
        try {
            // HttpUrl checks url more strictly than java.net.URL.
            HttpUrl hurl = HttpUrl.parse(str);

            URL url = hurl.url();
            return url;
        } catch (Exception e) {
            return null;
        }
    }

    public static URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URI toURI(String url) {
        return toURI(toURL(url));
    }

    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static boolean isURL(String str) {
        return toURL(str) != null;
    }

    public static String getHost(String url) {
        try {
            HttpUrl hurl = HttpUrl.parse(url);
            return hurl.host();
        } catch (Throwable e) {
            return null;
        }
    }

    public static String getHostUrl(String url) {
        try {
            HttpUrl hurl = HttpUrl.parse(url);
            return hurl.scheme() + "://" + hurl.host();
        } catch (Throwable e) {
            return null;
        }
    }

    public static String urlEncode(String v, String charset) {
        try {
            return URLEncoder.encode(v, charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String urlEncode(String v) {
        return urlEncode(v, "UTF-8");
    }

    public static void addLastModifiedHeader(Request.Builder builder, Date lastModifiedSince) {
        if (lastModifiedSince != null && lastModifiedSince.getTime() > 0) {
            builder.header("If-Modified-Since", formatDate(lastModifiedSince));
        }
    }

    public static String formatDate(Date date) {
        return HttpDate.format(date);
    }

    public static Request.Builder createRequestBuilder(HttpUrl url, Date lastModifiedSince) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        addLastModifiedHeader(builder, lastModifiedSince);
        return builder;
    }

    public static Request createRequest(HttpUrl url, Date lastModifiedSince) {
        return createRequestBuilder(url, lastModifiedSince).build();
    }

    public static String getPathExtension(HttpUrl url) {
        List<String> segments = url.pathSegments();
        if (segments.size() >= 1) {
            String name = segments.get(segments.size() - 1);
            return StringUtils.getExtension(name);
        }
        return "";
    }

    public static void printCookieStore(CookieStore store) {
        for (HttpCookie c : store.getCookies()) {
            JUtils.println(String.format("%s: %s [%s : %s]", c.getDomain(), c.getPath(), c.getName(), c.getValue()));
        }
    }
}

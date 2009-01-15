/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.curiouscreature.android.shelves.util;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.content.Context;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;

import java.io.IOException;

public class CookieStore {
    private static final String LOG_TAG = "Shelves";

    private static final CookieStore sCookieStore;
    static {
        sCookieStore = new CookieStore();
    }

    private CookieStore() {
    }

    public static void initialize(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().removeExpiredCookie();
    }

    public static CookieStore get() {
        return sCookieStore;
    }

    public String getCookie(String url) {
        final CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(url);

        if (cookie == null || cookie.length() == 0) {
            final HttpHead head = new HttpHead(url);
            HttpEntity entity = null;
            try {
                final HttpResponse response = HttpManager.execute(head);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    entity = response.getEntity();

                    final Header[] cookies = response.getHeaders("set-cookie");
                    for (Header cooky : cookies) {
                        cookieManager.setCookie(url, cooky.getValue());
                    }

                    CookieSyncManager.getInstance().sync();
                    cookie = cookieManager.getCookie(url);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not retrieve cookie", e);
            } finally {
                if (entity != null) {
                    try {
                        entity.consumeContent();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Could not retrieve cookie", e);                        
                    }
                }
            }
        }

        return cookie;
    }
}
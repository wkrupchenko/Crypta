package com.crypta.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;

import java.util.Locale;

/**
 * Singleton instance of DbxClientV2
 */
public class DropboxClientFactory {

    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            String userLocale = Locale.getDefault().toString();
            DbxRequestConfig requestConfig = new DbxRequestConfig(
                    "Crypta/1.2.0",
                    userLocale,
                    OkHttpRequestor.INSTANCE);
            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }

    public static void reinitialize(String accessToken) {
        sDbxClient = null;
        String userLocale = Locale.getDefault().toString();
        DbxRequestConfig requestConfig = new DbxRequestConfig(
                "Crypta/1.2.0",
                userLocale,
                OkHttpRequestor.INSTANCE);
        sDbxClient = new DbxClientV2(requestConfig, accessToken);
    }
}

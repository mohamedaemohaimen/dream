package com.tt.dramatime.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tt.dramatime.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author WJ
 */
public class ReferrerUtils {

    /**
     * 解析install referrer 信息
     * 示例：utm_source=LP&utm_content=dramatime%3Aplayvideo(sourceCode%3D0LRO78%26traceId%3D20240515144533-bc7a552577ea4207b7df3897d3ba591f%26lang%3Dja%26pixel%3D8107690039244266%26adId%3D%22%22)
     */
    public static Map<String, String> parseQueryString(String queryString) {
        String enc = "UTF-8";
        Map<String, String> params = new HashMap<>();
        if (queryString.isEmpty()) {
            return params;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = null;
            String value = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            } else {
                try {
                    key = URLDecoder.decode(pair.substring(0, idx), enc);
                    value = URLDecoder.decode(pair.substring(idx + 1), enc);
                } catch (UnsupportedEncodingException ignored) {
                }
            }
            if (key != null && value != null) {
                params.put(key, value);
            }
        }
        return params;
    }

    private static final Pattern DATA_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    /**
     * 解析utm_content数据
     */
    public static Map<String, String> parseData(String data) {
        if (data.isEmpty()) {
            return null;
        }
        Matcher m = DATA_PATTERN.matcher(data);
        if (!m.find()) {
            return null;
        }
        // 使用ConcurrentHashMap以支持并发修改
        Map<String, String> map = new ConcurrentHashMap<>();
        String params = m.group(1);
        String[] keyValuePairs = new String[0];
        if (params != null) {
            keyValuePairs = params.split("&");
        }

        for (String pair : keyValuePairs) {
            // 指定limit为2，以避免数组索引越界
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length < 2) {
                // 如果没有等号，跳过这个键值对
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * 获取FaceBook InstallReferrer
     */
    public static String getFaceBookInstallReferrer(Context context) {
        String appId = context.getString(R.string.facebook_app_id);
        Cursor c = null;
        try {
            String[] projection = {"install_referrer", "is_ct", "actual_timestamp",};
            Uri providerUri;

            // This IF statement queries the facebook app first.
            // To query from the instagram app first, change the sequence of the IF statement
            if (context.getPackageManager().resolveContentProvider(
                    "com.facebook.katana.provider.InstallReferrerProvider", 0) != null) {
                providerUri = Uri.parse(
                        "content://com.facebook.katana.provider.InstallReferrerProvider/" + appId);
            } else if (context.getPackageManager().resolveContentProvider(
                    "com.instagram.contentprovider.InstallReferrerProvider", 0) != null) {
                providerUri = Uri.parse(
                        "content://com.instagram.contentprovider.InstallReferrerProvider/" + appId);
            } else {
                return "";
            }

            c = context.getContentResolver().query(providerUri, projection, null, null, null);
            if (c == null || !c.moveToFirst()) {
                return "";
            }

            int installReferrerIndex = c.getColumnIndex("install_referrer");
            int timestampIndex = c.getColumnIndex("actual_timestamp");
            int isCTIndex = c.getColumnIndex("is_ct");
            // serialized and encrypted attribution details
            String installReferrer = c.getString(installReferrerIndex);

            // 1. deserialize installReferrer (convert String to JSON)
            // 2. decrypt attribution details in install_referrer.utm_content.source.data using
            // key: GPIR decryption key
            // nonce: retrieve from installReferrer.utm_content.source.nonce
            // data: retrieve from installReferrer.utm_content.source.data
            // decrypt data
            // timestamp in seconds for click/impression
            long actualTimestamp = c.getLong(timestampIndex);
            // VT or CT, 0 = VT, 1 = CT
            int isCT = c.getInt(isCTIndex);
            return installReferrer;
        } catch (Exception e) {
            return "";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

}

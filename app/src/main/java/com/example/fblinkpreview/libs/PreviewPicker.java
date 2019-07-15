package com.example.fblinkpreview.libs;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * Copyright (C) 2019 CloudLoopR - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 **/
public class PreviewPicker {

    private static PreviewPicker previewPicker;
    private List<PreviewCallback> previewCallbacks;

    private final String HTTP_PROTOCOL = "http://";
    private final String HTTPS_PROTOCOL = "https://";

    static {
        previewPicker = new PreviewPicker();
    }

    private PreviewPicker() {
        previewCallbacks = new ArrayList<>();
    }

    public static PreviewPicker getInstance() {
        return previewPicker;
    }

    public void startPicking(String url, PreviewCallback previewCallback) {

        try {
            PickerTask pickerTask = new PickerTask(url, previewCallback);
            pickerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class PickerTask extends AsyncTask<Void, Void, Preview> {

        private String url;
        private PreviewCallback previewCallback;

        public PickerTask(String url, PreviewCallback previewCallback) {
            this.url = url;
            this.previewCallback = previewCallback;
        }

        @Override
        protected Preview doInBackground(Void... voids) {
            return startOperation(url);
        }

        @Override
        protected void onPostExecute(Preview preview) {
            super.onPostExecute(preview);

            if (preview != null) {
                previewCallback.onSuccess(preview);
            } else {
                previewCallback.onError();
            }
        }
    }

    private Preview startOperation(String url) {

        try {
            String operationUrl = operationUrl(url);

            if (!TextUtils.isEmpty(operationUrl)) {

                Document doc = getDocument(operationUrl);
                String htmlCodes = PreviewUtil.extendedTrim(doc.toString());

                HashMap<String, String> metaTags = getMetaTags(htmlCodes);

                ImagePickingStrategy imagePickStrategy = new ImagePickingStrategy();
                List<String> images = imagePickStrategy.getImages(doc, metaTags);

                String title = doc.title();
                if (TextUtils.isEmpty(title)) {
                    title = metaTags.get("title");

                    if (TextUtils.isEmpty(title)) {
                        title = Regex.pregMatch(htmlCodes, Regex.TITLE_PATTERN, 2);
                        if (!TextUtils.isEmpty(title)) {
                            title = htmlDecode(title);
                        }
                    }
                }

                String cannonicalUrl = cannonicalUrl(operationUrl);

                return new Preview().setImages(images)
                        .setTitle(title).setLink(cannonicalUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String operationUrl(final String originURL) {
        String finalUrl = "";

        try {
            URL operationUrl = new URL(originURL);

            URLConnection urlConn = operationUrl.openConnection();
            Map<String,List<String>> map = urlConn.getHeaderFields();

            if (map != null && map.size() > 0) {

                if (map.containsKey("Location")) {
                    List<String> locationValue = map.get("Location");

                    if (locationValue != null && locationValue.size() > 0) {
                        finalUrl = locationValue.get(0);
                    } else {
                        finalUrl = originURL;
                    }

                } else {
                    finalUrl = originURL;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalUrl;
    }

    private Document getDocument(String operationUrl) throws IOException {
        return Jsoup.connect(operationUrl)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .get();
    }

    private HashMap<String, String> getMetaTags(String htmlCodes) {

        HashMap<String, String> metaTags = new HashMap<>();
        metaTags.put("url", "");
        metaTags.put("title", "");
        metaTags.put("description", "");
        metaTags.put("image", "");

        List<String> matches = Regex.pregMatchAll(htmlCodes, Regex.METATAG_PATTERN, 1);

        for (String match : matches) {

            final String lowerCase = match.toLowerCase();

            if (lowerCase.contains("property=\"og:url\"")
                    || lowerCase.contains("property='og:url'")
                    || lowerCase.contains("name=\"url\"")
                    || lowerCase.contains("name='url'")) {

                metaTags.put("url", separeMetaTagsContent(match));

            } else if (lowerCase.contains("property=\"og:title\"")
                    || lowerCase.contains("property='og:title'")
                    || lowerCase.contains("name=\"title\"")
                    || lowerCase.contains("name='title'")) {

                metaTags.put("title", separeMetaTagsContent(match));

            } else if (lowerCase.contains("property=\"og:description\"")
                    || lowerCase.contains("property='og:description'")
                    || lowerCase.contains("name=\"description\"")
                    || lowerCase.contains("name='description'")) {

                metaTags.put("description", separeMetaTagsContent(match));

            } else if (lowerCase.contains("property=\"og:image\"")
                    || lowerCase.contains("property='og:image'")
                    || lowerCase.contains("name=\"image\"")
                    || lowerCase.contains("name='image'")) {

                metaTags.put("image", separeMetaTagsContent(match));
            }
        }

        return metaTags;
    }

    private String separeMetaTagsContent(String content) {
        String data = Regex.pregMatch(content, Regex.METATAG_CONTENT_PATTERN, 1);
        return htmlDecode(data);
    }

    /** Transforms from html to normal string */
    private String htmlDecode(String content) {
        return Jsoup.parse(content).text();
    }

    private String cannonicalUrl(String url) {

        String cannonical = "";
        if (url.startsWith(HTTP_PROTOCOL)) {
            url = url.substring(HTTP_PROTOCOL.length());
        } else if (url.startsWith(HTTPS_PROTOCOL)) {
            url = url.substring(HTTPS_PROTOCOL.length());
        }

        int urlLength = url.length();
        for (int i = 0; i < urlLength; i++) {
            if (url.charAt(i) != '/')
                cannonical += url.charAt(i);
            else
                break;
        }

        return cannonical;

    }
}

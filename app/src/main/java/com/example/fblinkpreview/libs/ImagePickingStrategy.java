package com.example.fblinkpreview.libs;


import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ============================================================================
 * Copyright (C) 2019 CloudLoopR - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 **/
public class ImagePickingStrategy {

    private List<String> getMetaImage(HashMap<String, String> metaTags) {
        List<String> images = new ArrayList<>();
        final String metaImage = metaTags.get("image");

        if (!TextUtils.isEmpty(metaImage)) {
            images.add(metaImage);
        }
        return images;
    }

    private List<String> getImagesFromImgTags(Document document) {
        List<String> images = new ArrayList<>();
        Elements media = document.select("[src]");

        for (Element srcElement : media) {
            if (srcElement.tagName().equals("img")) {
                images.add(srcElement.attr("abs:src"));
            }
        }
        return images;
    }

    List<String> getImages(Document document, HashMap<String, String> metaTags) {
        List<String> images = getMetaImage(metaTags);

        if (images.isEmpty()) {
            images.addAll(getImagesFromImgTags(document));
        }
        return images;
    }

}

package com.example.fblinkpreview.libs;


/**
 * ============================================================================
 * Copyright (C) 2019 CloudLoopR - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 **/
public class PreviewUtil {

    /** Removes extra spaces and trim the string */
    public static String extendedTrim(String content) {
        return content.replaceAll("\\s+", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }
}

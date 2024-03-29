package com.example.fblinkpreview.library.url;

import java.util.List;

/**
 * Provides the means for extracting URL(s) from text.
 */
public interface UrlExtractionStrategy {
    List<String> extractUrls(String textPassedToTextCrawler);
}

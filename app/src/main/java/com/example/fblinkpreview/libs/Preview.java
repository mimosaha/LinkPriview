package com.example.fblinkpreview.libs;


import java.util.List;

/**
 * ============================================================================
 * Copyright (C) 2019 CloudLoopR - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 **/
public class Preview {
    private String title, description, link;
    private List<String> images;

    public String getTitle() {
        return title;
    }

    public Preview setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Preview setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<String> getImage() {
        return images;
    }

    public Preview setImages(List<String> images) {
        this.images = images;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Preview setLink(String link) {
        this.link = link;
        return this;
    }
}

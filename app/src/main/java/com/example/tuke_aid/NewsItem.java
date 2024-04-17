package com.example.tuke_aid;

public class NewsItem {

    private String title;
    private String content;

    public NewsItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
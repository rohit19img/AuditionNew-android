package com.img.audition.StoryView;

import java.io.Serializable;
import java.util.Date;

public class MyStory implements Serializable {

    private String url;

    private Date date;

    private String description;
    private int count;

    public MyStory(String url, Date date, String description, int count) {
        this.url = url;
        this.date = date;
        this.description = description;
        this.count = count;
    }

    public MyStory(String url, Date date) {
        this.url = url;
        this.date = date;
    }

    public MyStory(String url, int count) {
        this.url = url;
        this.count = count;
    }

    public MyStory() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

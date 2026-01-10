package com.nazarukiv.csstudentshelperbot.service.resources;

public class ResourceItem {
    private final String title;
    private final String url;
    private final String note;

    public ResourceItem(String title, String url, String note){
        this.title = title;
        this.url = url;
        this.note = note;
    }

    //getters
    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return title + "\n" +
                url + "\n" +
                note;
    }

}

package com.crypta.gui.adapter;

/**
 * Created by D064343 on 14.07.2016.
 */
public class Provider {
    private String title;

    public Provider() {
    }

    public Provider(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Provider provider = (Provider) o;

        return title.equals(provider.title);

    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return "Provider{" +
                "title='" + title + '\'' +
                '}';
    }
}

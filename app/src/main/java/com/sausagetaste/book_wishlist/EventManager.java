package com.sausagetaste.book_wishlist;


import java.util.HashSet;
import java.util.Set;

public class EventManager {

    //// To make this a singleton

    private static EventManager inst = null;

    public static EventManager get_inst() {
        if (null == inst) {
            inst = new EventManager();
        }

        return inst;
    }


    // Definitions

    interface ImageDownloadedListener {
        void notify_image_downloaded();
    }

    interface HTMLLoadedListener {
        void notify_html_loaded(final String html);
    }


    // Attributes

    Set<ImageDownloadedListener> image_downloaded_observers;
    Set<HTMLLoadedListener> html_loaded_observers;


    // Methods

    private EventManager() {
        this.image_downloaded_observers = new HashSet<>();
        this.html_loaded_observers = new HashSet<>();
    }


    public void register_image_downloaded(final ImageDownloadedListener observer) {
        this.image_downloaded_observers.add(observer);
    }

    public void deregister_image_downloaded(final ImageDownloadedListener observer) {
        this.image_downloaded_observers.remove(observer);
    }

    public void notify_image_downloaded() {
        for (ImageDownloadedListener x : this.image_downloaded_observers) {
            x.notify_image_downloaded();
        }
    }


    public void register_html_loaded(final HTMLLoadedListener observer) {
        this.html_loaded_observers.add(observer);
    }

    public void deregister_html_loaded(final HTMLLoadedListener observer) {
        this.html_loaded_observers.remove(observer);
    }

    public void notify_html_loaded(final String html) {
        for (HTMLLoadedListener x : this.html_loaded_observers) {
            x.notify_html_loaded(html);
        }
    }

}

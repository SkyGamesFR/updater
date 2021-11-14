package fr.darki.updater;

import java.io.File;

public class Updater {
    private String url;
    private String file;
    private File dir;


    public Updater(String url, String file, File dir) {
        this.url = url;
        this.dir = dir;
    }
}

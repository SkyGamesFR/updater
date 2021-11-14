package fr.darki.updater;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import fr.darki.updater.json.Connection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Updater {
    private static String url;
    private static String folder;
    private static File dir;
    private static JSONArray files = null;
    private static JSONArray ignore = null;
    private static Connection connection;


    public Updater(String url, String folder, File dir) {
        this.url = url;
        this.folder = folder;
        this.dir = dir;
        this.connection = new Connection(this.url);
    }

    private void init() throws IOException {
        System.out.println("SkyGames Updater by DarKi");

    }

}

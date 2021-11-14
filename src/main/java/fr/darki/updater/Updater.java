package fr.darki.updater;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import fr.darki.updater.json.Connection;
import fr.holo.chaundl.json.ConnectionJson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Updater {
    private static String url;
    private static String folder;
    private static File dir;
    private static Integer downloadedSize = 0;
    private static Integer totalSize = 0;
    private static JSONArray files = null;
    private static JSONArray ignore = null;

    public Updater(String url, String folder, File dir) {
        this.url = url;
        this.folder = folder;
        this.dir = dir;
        Connection.url = this.url;
    }

    private void init() throws IOException {
        System.out.println("SkyGames Updater by DarKi");

        try {
            JSONObject json = Connection.readJSON(folder);
            this.files = json.getJSONArray("files");
            this.ignore = json.getJSONArray("ignore");
        } catch (JSONException | IOException err) {
            err.printStackTrace();
        }

        int i;
        for(i = 0; i < this.files.length(); ++i) {
            try {
                totalSize += this.files.getJSONObject(i).getInt("size");
            } catch (JSONException err) {
                err.printStackTrace();
            }
        }

        if (this.ignore.length() != 0) {
            System.out.println("************IGNORE_LIST************\n");
            for(i = 0; i < this.ignore.length(); ++i) {
                try {
                    System.out.println("- " + this.ignore.getJSONObject(i).getString("path"));
                } catch (JSONException err) {
                    err.printStackTrace();
                }
            }
            System.out.println("***********************************");
        }
    }

    public void start() throws IOException {
        if (!this.dir.exists()) {
            this.dir.mkdirs();
        }

        this.init();

        for(int i = 0; i < this.files.length(); ++i) {
            File Hdir = null;


        }
    }


    private static void downloadFile(String path) throws IOException {
        String dlUrl = url.substring(url.length() - 1).equals("/") ? url: (url + "/") + ConnectionJson.folder + "/" + path;

        System.out.println("[SkyGames Updater] Downloading: " + path);
        InputStream is = null;
        FileOutputStream fos = null;
        String outputPath = dir + File.separator + path;
        URL u = new URL(dlUrl);

        try {
            URLConnection urlConn = u.openConnection();
            is = urlConn.getInputStream();
            fos = new FileOutputStream(outputPath);
            byte[] buffer = new byte[4096];

            int length;
            while((length = is.read(buffer)) > 0) {
                downloadedSize += length;
                fos.write(buffer, 0, length);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }

            }

        }

    }
}

package fr.darki.updater;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.darki.updater.json.Connection;
import fr.holo.chaundl.json.ConnectionJson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Updater {
    private static String url;
    private static String folder;
    private static File dir;
    private static Integer currentDlSize = 0;
    private static Integer downloadedSize = 0;
    private static Integer totalSize = 0;
    private static JSONArray files = null;
    private static JSONArray ignore = null;
    private static List<String> allowedFiles= new ArrayList();;

    public Updater(String url, String folder, String dir) {
        this.url = url;
        this.folder = folder;
        this.dir = new File(folderSeparator(dir));
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
                allowedFiles.add(this.files.getJSONObject(i).getString("path"));
                totalSize += this.files.getJSONObject(i).getInt("size");
            } catch (JSONException err) {
                err.printStackTrace();
            }
        }

        if (this.ignore.length() != 0) {
            System.out.println("************IGNORE_LIST************\n");
            for(i = 0; i < this.ignore.length(); ++i) {
                try {
                    allowedFiles.add(this.ignore.getJSONObject(i).getString("path"));
                    System.out.println("- " + this.ignore.getJSONObject(i).getString("path"));
                } catch (JSONException err) {
                    err.printStackTrace();
                }
            }
            System.out.println("***********************************");
        }
    }

    public void start() throws Exception {
        if (!this.dir.exists()) {
            this.dir.mkdirs();
        }

        this.init();

        for(int i = 0; i < this.files.length(); ++i) {
            try {
                JSONObject json = this.files.getJSONObject(i);
                String path = json.getString("path");
                String checksum = json.getString("md5");
                long size = json.getInt("size");
                File file = new File(this.dir, path);

                while (!file.exists() || Checksum.compare(path, checksum) || file.length() != size) {
                    if (file.exists()) file.delete();
                    downloadFile(path);
                }
            } catch (JSONException | IOException err) {
                err.printStackTrace();
            }

            downloadedSize += currentDlSize;
            currentDlSize = 0;
        }

        checkFiles();
    }

    public int getTotalSize() { return totalSize; }

    public int getDownloadedSize() { return downloadedSize; }

    public int getCurrentDlSize() { return currentDlSize; }

    public static String getStatus(String url) {
        JSONObject json;
        try {
            json = Connection.readJSON("status");
            try {
                Boolean status = json.getBoolean("active");
                if (status == true) return json.getString("message");
                else return null;
            } catch (JSONException err) {
                err.printStackTrace();
            }
        } catch (JSONException | IOException err) {
            err.printStackTrace();
        }
        return null;
    }

    private static String folderSeparator(String path) {
        int count = 0;
        String result = "";
        for(int i = 0; i < path.length(); ++i) {
            if (path.charAt(i) == '/') {
                result = result + "\\" + path.split("/")[count];
                ++count;
            }
        }
        return result;
    }

    private static void folderMkdir(String path) {
        File f = new File(dir,  folderSeparator(path));
        f.mkdirs();
    }

    private static void checkFiles() {
        try {
            Stream walk = Files.walk(Paths.get(dir.getAbsolutePath()));

            try {
                List<String> result = (List)walk.filter((var0) -> {
                    return Files.isRegularFile((Path) var0, new LinkOption[0]);
                }).map((x) -> {
                    return x.toString();
                }).collect(Collectors.toList());
                result.forEach((e) -> {
                    if (!allowedFiles.contains(e)) {
                        (new File(e)).delete();
                    }
                });
                int i;
                for(i = 0; i < ignore.length(); ++i) {
                    try {
                        JSONObject json = ignore.getJSONObject(i);
                        if (json.getString("type") == "file") {
                            File file = new File(dir, folderSeparator(json.getString("path")));
                            if (!Checksum.compare(json.getString("path"), json.getString("md5")) || file.length() != json.getInt("size")) {
                                file.delete();
                            }
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            } finally {
                if (walk != null) {
                    walk.close();
                }

            }
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private static void downloadFile(String path) throws IOException {
        String dlUrl = url.substring(url.length() - 1).equals("/") ? url: (url + "/") + ConnectionJson.folder + "/" + path;

        System.out.println("[SkyGames Updater] Downloading: " + path);
        InputStream is = null;
        FileOutputStream fos = null;
        String outputPath = dir + File.separator + path;
        URL u = new URL(dlUrl);

        folderMkdir(path);

        try {
            URLConnection urlConn = u.openConnection();
            is = urlConn.getInputStream();
            fos = new FileOutputStream(outputPath);
            byte[] buffer = new byte[4096];

            int length;
            while((length = is.read(buffer)) > 0) {
                currentDlSize += length;
                fos.write(buffer, 0, length);
            }
        } finally {
            try {
                if (is != null) is.close();
            } finally {
                if (fos != null) fos.close();
            }
        }
    }
}

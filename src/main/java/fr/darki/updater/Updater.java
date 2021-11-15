package fr.darki.updater;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.darki.updater.json.Connection;
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
    private static final Map<String, JSONObject> allowedFiles = new HashMap<>();

    public Updater(String url, String folder, String dir) {
        Updater.url = url;
        Updater.folder = folder;
        Updater.dir = new File(folderSeparator(dir));
        Connection.url = Updater.url;
    }

    private void init() {
        System.out.println("SkyGames Updater by DarKi");

        try {
            JSONObject json = Connection.readJSON(folder);
            files = json.getJSONArray("files");
            ignore = json.getJSONArray("ignore");
        } catch (JSONException | IOException err) {
            err.printStackTrace();
        }

        int i;
        for(i = 0; i < files.length(); ++i) {
            try {
                allowedFiles.put(files.getJSONObject(i).getString("path"), files.getJSONObject(i));
                totalSize += files.getJSONObject(i).getInt("size");
            } catch (JSONException err) {
                err.printStackTrace();
            }
        }

        if (ignore.length() != 0) {
            System.out.println("************IGNORE_LIST************\n");
            for(i = 0; i < ignore.length(); ++i) {
                try {
                    allowedFiles.put(ignore.getJSONObject(i).getString("path"), ignore.getJSONObject(i));
                    System.out.println("- " + ignore.getJSONObject(i).getString("path"));
                } catch (JSONException err) {
                    err.printStackTrace();
                }
            }
            System.out.println("***********************************");
        }
    }

    public void start() throws Exception {
        if (!dir.exists()) {
            if(!dir.mkdirs()) System.out.println("An error occurred, failed to create directory " + dir.getAbsolutePath());
        }

        this.init();

        for(int i = 0; i < files.length(); ++i) {
            try {
                JSONObject json = files.getJSONObject(i);
                String path = json.getString("path");
                String checksum = json.getString("md5");
                long size = json.getInt("size");
                File file = new File(dir, path);

                while (!file.exists() || Checksum.compare(path, checksum) || file.length() != size) {
                    if (file.exists()) {
                        if (!file.delete()) System.out.println("An error occurred, could not delete " + file.getAbsolutePath());
                    }
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
                boolean status = json.getBoolean("active");
                if (status) return json.getString("message");
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
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < path.length(); ++i) {
            if (path.charAt(i) == '/') {
                result.append("\\").append(path.split("/")[count]);
                ++count;
            }
        }
        return result.toString();
    }

    private static void folderMkdir(String path) {
        File f = new File(dir,  folderSeparator(path));
        if (!f.mkdirs()) System.out.println("An error occurred");
    }

    private static void checkFiles() {
        try {
            try (Stream<Path> walk = Files.walk(Paths.get(dir.getAbsolutePath()))) {
                List<Path> result;
                result = walk.filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                result.forEach((e) -> {
                    String str = String.valueOf(e);
                    File file = new File(str);
                    if (!allowedFiles.containsKey(str)) {
                        if (!file.delete()) System.out.println("An error occurred");
                    } else {
                        JSONObject json = allowedFiles.get(str);
                        if (Objects.equals(json.getString("type"), "file")) {
                            try {
                                if (!Checksum.compare(json.getString("path"), json.getString("md5")) || file.length() != json.getInt("size")) {
                                    if (!file.delete()) System.out.println("An error occurred");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private static void downloadFile(String path) throws IOException {
        String dlUrl = url.endsWith("/") ? url: (url + "/") + folder + "/" + path;
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

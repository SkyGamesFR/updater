package fr.darki.updater.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class Connection {
    public static String url;

    public Connection(String url) {
        this.url = url;
    }

    private static String readAll(Reader rd)throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while((cp = rd.read()) != -1) {
            sb.append((char)cp);
        }
        return sb.toString();
    }

    public static JSONObject readJSONFromUrl(String path) throws IOException, JSONException {
        String dlURL;
        dlURL = url.substring(url.length() - 1).equals("/") ? url.substring(0, url.length() - 1): url + "/" + path + "/";

        InputStream is = (new URL(dlURL)).openStream();

        JSONObject res;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = Connection.readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            res = json;
        } finally {
            is.close();
        }

        return res;
    }

}

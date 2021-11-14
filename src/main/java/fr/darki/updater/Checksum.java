package fr.darki.updater;

import java.io.*;
import java.security.MessageDigest;
import java.util.Objects;

public class Checksum {
    public Checksum() {
    }

    public static void main(String[] args) {
    }

    private static byte[] create(String path) throws Exception {
        InputStream fis =  new FileInputStream(path);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String get(String path) throws Exception {
        byte[] b = create(path);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

    public static Boolean check(String path, String checksum) throws Exception {
        if (Objects.equals(get(path), checksum)) {
            return true;
        }
        return false;
    }
}

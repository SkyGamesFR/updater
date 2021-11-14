package fr.darki.updater;

import java.io.*;
import java.security.MessageDigest;
import java.util.Objects;

public class Checksum {
    public Checksum(String path) {
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
        StringBuilder result = new StringBuilder();

        for (int i : b) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }

        return result.toString();
    }

    public static Boolean compare(String path, String checksum) throws Exception {
        return Objects.equals(get(path), checksum);
    }
}

package com.DwqeGroup.tools;

import java.io.*;
import java.util.ArrayList;

public class FileBytesList {
    private static final int BYTES_LEN = 4;
    public static ArrayList<byte[]> ReadFile(String fileName) {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        try {
            FileInputStream fileInput = new FileInputStream(fileName);
            int n = fileInput.read();
            if (n == -1) {
                System.out.println("Empty file");
            }
            else {
                int cnt = 0;
                byte[] bytes = new byte[BYTES_LEN];
                do {
                    bytes[cnt % BYTES_LEN] = (byte)n;
                    cnt++;
                    if (cnt % BYTES_LEN == 0) {
                        arrayList.add(bytes);
                        bytes = new byte[4];
                    }
                    n = fileInput.read();
                } while (n != -1);
                if (cnt % BYTES_LEN != 0) {
                    arrayList.add(bytes);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static void WriteFile(String fileName,  ArrayList<byte[]> arrayList) {
        File fileOutput = new File(fileName);
        if(!fileOutput.exists()){
            try {
                boolean createFlag = fileOutput.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileOutput);
            for (byte[] bytes : arrayList) {
                fileOutputStream.write(bytes);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}

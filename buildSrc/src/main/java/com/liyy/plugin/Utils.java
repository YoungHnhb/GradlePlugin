package com.liyy.plugin;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.zip.ZipFile;

/**
 * Author: 李岳阳
 * Date: 11-9
 * Time: 16:22
 * Description：
 */
public class Utils {

    public static int BUFFER_SIZE = 16384;

    public static String readFileAsString(String filePath) {
        StringBuilder fileData = new StringBuilder();
        Reader fileReader = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            fileReader = new InputStreamReader(inputStream, "UTF-8");
            char[] buf = new char[BUFFER_SIZE];
            int numRead = fileReader.read(buf);
            while (numRead != -1) {
                String readData = new String(buf, 0, numRead);
                fileData.append(readData);
                numRead = fileReader.read(buf);
            }
        } catch (Exception e) {

        } finally {
            try {
                closeQuietly(fileReader);
                closeQuietly(inputStream);
            } catch (Exception e) {
            }
        }
        return fileData.toString();
    }


    private static void closeQuietly(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            if (obj instanceof Closeable) {
                ((Closeable) obj).close();
            } else if (obj instanceof AutoCloseable) {
                ((AutoCloseable) obj).close();
            } else if (obj instanceof ZipFile) {
                ((ZipFile)obj).close();
            } else {
                throw new IllegalArgumentException("obj $obj is not closeable");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void listClassFiles(ArrayList<File> classFiles, File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file == null) {
                continue;
            }
            if (file.isDirectory()) {
                listClassFiles(classFiles, file);
            } else {
                if (null != file && file.isFile()) {
                    classFiles.add(file);
                }

            }
        }
    }

}

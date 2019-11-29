package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Common {

    public static void main(String[] args) {
        String test = "[]";
        List<String> result = stringToList(test);
        System.out.println(test);
    }

    // "[a,b,c]" to [a,b,c]
    public static List<String> stringToList(String str) {
        List<String> result = null;
        str = str.substring(1, str.length() - 1);
        if (str.length() == 0) {
            result = new ArrayList<String>();
        }
        else {
            result = Arrays.asList(str.split(","));
        }
        return result;
    }

    public  static String readTextFile(String filePath) {
        File file = new File(filePath);
        Long fileLength = file.length();
        if (fileLength > fileLength.intValue()) {
            System.out.println("Caution!!! FileLength exceed MAX INT value!");
        }
        byte[] fileContent = new byte[fileLength.intValue()];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileContent);
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = new String(fileContent);
        return content;
    }
}

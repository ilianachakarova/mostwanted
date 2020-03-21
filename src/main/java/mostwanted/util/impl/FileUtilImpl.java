package mostwanted.util.impl;

import mostwanted.util.FileUtil;

import java.io.*;

public class FileUtilImpl implements FileUtil {
    @Override
    public String readFile(String filePath) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
        String line;

        while((line = reader.readLine()) != null){
            fileContent.append(line).append(System.lineSeparator());
        }
        return fileContent.toString().trim();
    }
}

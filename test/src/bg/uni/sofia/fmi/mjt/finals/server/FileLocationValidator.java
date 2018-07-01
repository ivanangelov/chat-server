package bg.uni.sofia.fmi.mjt.finals.server;

import java.nio.file.Paths;

class FileLocationValidator {
    static boolean isValidLocation(String path) {
        if (!path.contains("/")) {
            return false;
        }
        String newFilePath = "";
        int index = path.lastIndexOf("/");
        if (index > 0) {
            newFilePath = path.substring(0, index);
        }

        return Paths.get(newFilePath).toFile().isDirectory();
    }
}

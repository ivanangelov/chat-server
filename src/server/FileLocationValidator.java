package bg.uni.sofia.fmi.mjt.finals.server;

import java.nio.file.Paths;

/**
 * Represents a file or directory path validator.
 */
class FileLocationValidator {
    /**
     * Validates a file or directory by path.
     * @param path the path of the file to be validated
     * @return true if path is valid location; false otherwise
     */
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

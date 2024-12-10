package model;

/**
 * An intermediate class used to store parsed file data from byte array sent from
 * the client.
 */
public class FileData {
    private final String fileName;
    private final byte[] fileContent;

    public FileData(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}

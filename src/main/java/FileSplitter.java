import java.io.*;
import java.util.List;
import java.util.ArrayList;

class FileChunk {
    private final String fileName;
    private final long startOffset;
    private final long endOffset;
    private final byte[] data;
    private final int chunkIndex;

    public FileChunk(String fileName, long startOffset, long endOffset, byte[] data, int index) {
        this.fileName = fileName;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.data = data;
        this.chunkIndex = index;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return endOffset - startOffset;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public byte[] getData() {
        return data;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }
}

public class FileSplitter {
    public final static int kChunkSize = 2048 * 1024; // 2MB

    static public List<FileChunk> splitFile(String fileName) {
        List<FileChunk> chunks = new ArrayList<>();
        File file = new File(fileName);

        try (FileInputStream fis = new FileInputStream(file)) {
            long fileSize = file.length();
            long bytesRead = 0;
            int chunkIndex = 0;

            while (bytesRead < fileSize) {
                int currentChunkSize = (int) Math.min(kChunkSize, fileSize - bytesRead);
                byte[] buffer = new byte[currentChunkSize];

                int readBytes = fis.read(buffer);
                if (readBytes != -1) {
                    long startOffset = (long) chunkIndex * kChunkSize;
                    long endOffset = startOffset + readBytes - 1;

                    FileChunk chunk = new FileChunk(fileName, startOffset, endOffset, buffer, chunkIndex);
                    chunks.add(chunk);

                    bytesRead += readBytes;
                    chunkIndex++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return chunks;
    }
}
package service;

import client.SessionScopedJPaxosClient;
import common.FileDTO;
import common.RequestType;
import lsr.paxos.client.ReplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class FileListService {

    private final SessionScopedJPaxosClient sessionScopedJPaxosClient;

    @Autowired
    public FileListService(@Lazy SessionScopedJPaxosClient sessionScopedJPaxosClient) {
        this.sessionScopedJPaxosClient = sessionScopedJPaxosClient;
    }

    /**
     * List all files.
     *
     * @return List of files.
     */
    public List<FileDTO> getAllFiles() {
        try {
            byte[] dataToSend = prepareListRequest();
            byte[] listOfFiles = sessionScopedJPaxosClient.getClient().execute(dataToSend);
            return parseListOfFiles(listOfFiles);
        } catch(IOException | ClassNotFoundException | ReplicationException e) {
            e.printStackTrace();
            throw new RuntimeException("List files failed: " + e.getMessage());
        }
    }

    /**
     * Prepare list request.
     *
     * @return List request in byte array form.
     * @throws IOException Throws IOException.
     */
    private byte[] prepareListRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(RequestType.LIST.getCode());
        return baos.toByteArray();
    }

    /**
     * Parse byte array of list of file metadata.
     *
     * @param listOfFiles List of file metadata in byte array form.
     * @return List of file DTO.
     * @throws IOException Throws IOException.
     * @throws ClassNotFoundException Throws ClassNotFoundException.
     */
    @SuppressWarnings("unchecked")
    private List<FileDTO> parseListOfFiles(byte[] listOfFiles)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(listOfFiles);
        ObjectInputStream ois = new ObjectInputStream(bais);
        List<FileDTO> fileDTOs = (List<FileDTO>) ois.readObject();
        ois.close();
        return fileDTOs;
    }
}


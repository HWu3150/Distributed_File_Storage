package service;

import db.DBClient;
import db.FileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileListService {

    private final DBClient dbClient;

    @Autowired
    public FileListService(DBClient dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * Get the list of all file names from the database.
     *
     * @return List of file names
     */
    public List<String> getAllFileNames() {
        return dbClient.getAll()
                .stream()
                .map(FileEntity::getFileName)
                .collect(Collectors.toList());
    }
}


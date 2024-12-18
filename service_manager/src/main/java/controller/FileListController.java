package controller;

import common.FileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.FileListService;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileListController {

    private final FileListService fileListService;

    @Autowired
    public FileListController(FileListService fileListService) {
        this.fileListService = fileListService;
    }

    /**
     * Endpoint to get the list of all files.
     *
     * @return List of file names
     */
    @GetMapping
    public List<FileDTO> getFileNames() {
        return fileListService.getAllFiles();
    }
}

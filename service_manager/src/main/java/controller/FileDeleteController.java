package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.FileDeleteService;

@RestController
@RequestMapping("/api")
public class FileDeleteController {

    private final FileDeleteService fileDeleteService;

    @Autowired
    public FileDeleteController(FileDeleteService fileDeleteService) {
        this.fileDeleteService = fileDeleteService;
    }

    @DeleteMapping("/delete")
    public String deleteFile(@RequestParam("fileId") int fileId) {
        try {
            return fileDeleteService.deleteFile(fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return "File deletion failed: " + e.getMessage();
        }
    }
}

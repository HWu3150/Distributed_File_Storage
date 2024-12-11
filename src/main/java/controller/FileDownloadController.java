package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.FileDownloadService;

@RestController
@RequestMapping("/api")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    @Autowired
    public FileDownloadController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileName") String fileName) {
        try{
            byte[] fileContent = fileDownloadService.downloadFile(fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment: filename=" + fileName);
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage().getBytes());
        }
    }
}

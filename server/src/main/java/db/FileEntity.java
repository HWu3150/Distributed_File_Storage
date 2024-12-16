package db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {
    private Integer id;
    private String fileName;
    private String fileType;
    private String fileDate;
    private Long fileSize;
    private String fileUrl;
}

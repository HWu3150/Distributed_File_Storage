package common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO implements Serializable {
    private Integer id;
    private String fileName;
    private String fileType;
    private String fileDate;
    private Long fileSize;
    private Integer isActive;
}

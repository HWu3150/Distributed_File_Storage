package common;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class FileDTO implements Serializable {
    private Integer id;
    private String fileName;
    private String fileType;
    private String fileDate;
    private Long fileSize;
    private Integer isActive;
}

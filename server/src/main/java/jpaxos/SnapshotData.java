package jpaxos;

import db.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotData implements Serializable {
    private File[] files;
    private List<FileEntity> fileEntities;
}

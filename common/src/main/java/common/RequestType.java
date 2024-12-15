package common;

import lombok.Getter;

@Getter
public enum RequestType {
    DOWNLOAD(0),
    UPLOAD(1),
    DELETE(2),
    UNKNOWN(-1);

    private final int code;

    RequestType(int code) {
        this.code = code;
    }
}

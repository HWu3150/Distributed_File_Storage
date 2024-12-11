import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Message {
  public enum OpCode {
    PUT, GET, DELETE, LIST
  }

  private OpCode op;
  private String fileName;
  private byte[] data;

  public Message(OpCode op, String fileName, byte[] data) {
    this.op = op;
    this.fileName = fileName;
    this.data = data;
  }

  public OpCode getOp() {
    return op;
  }

  public String getFileName() {
    return fileName;
  }

  public byte[] getData() {
    return data;
  }

  public byte[] toBytes() {
    byte[] fileNameBytes = fileName != null ? fileName.getBytes(StandardCharsets.UTF_8) : new byte[0];
    int fileNameLength = fileNameBytes.length;
    int dataLength = data != null ? data.length : 0;

    ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + fileNameLength + 4 + dataLength);
    buffer.put((byte) op.ordinal());
    buffer.putInt(fileNameLength);
    buffer.put(fileNameBytes);
    buffer.putInt(dataLength);
    if (dataLength > 0) {
      buffer.put(data);
    }

    return buffer.array();
  }

  static public Message fromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    OpCode op = OpCode.values()[buffer.get()];
    int fileNameLength = buffer.getInt();
    String fileName = null;
    if (fileNameLength > 0) {
      byte[] fileNameBytes = new byte[fileNameLength];
      buffer.get(fileNameBytes);
      fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    int dataLength = buffer.getInt();
    byte[] data = null;
    if (dataLength > 0) {
      data = new byte[dataLength];
      buffer.get(data);
    }

    return new Message(op, fileName, data);
  }
}

class PutResp {
  private boolean success;
  private String error;

  public PutResp(boolean success, String error) {
    this.success = success;
    this.error = error;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getError() {
    return error;
  }

  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + error.length());
    buffer.put((byte) (success ? 1 : 0));
    buffer.put((byte) error.length());
    buffer.put(error.getBytes(StandardCharsets.UTF_8));

    return buffer.array();
  }

  static public PutResp fromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    boolean success = buffer.get() == 1;
    int errorLength = buffer.get();
    byte[] errorBytes = new byte[errorLength];
    buffer.get(errorBytes);
    String error = new String(errorBytes, StandardCharsets.UTF_8);

    return new PutResp(success, error);
  }
}

class GetResp {
  private byte[] data;
  private boolean success;
  private String error;

  public GetResp(byte[] data, boolean success, String error) {
    this.data = data;
    this.success = success;
    this.error = error;
  }

  public byte[] getData() {
    return data;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getError() {
    return error;
  }

  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + data.length + 4 + error.length());
    buffer.put((byte) (success ? 1 : 0));
    buffer.putInt(data.length);
    buffer.put(data);
    buffer.putInt(error.length());
    buffer.put(error.getBytes(StandardCharsets.UTF_8));

    return buffer.array();
  }

  static public GetResp fromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    boolean success = buffer.get() == 1;
    int dataLength = buffer.getInt();
    byte[] data = new byte[dataLength];
    buffer.get(data);
    int errorLength = buffer.getInt();
    byte[] errorBytes = new byte[errorLength];
    buffer.get(errorBytes);
    String error = new String(errorBytes, StandardCharsets.UTF_8);

    return new GetResp(data, success, error);
  }
}

class ListResp {
  private ArrayList<String> files;

  public ListResp(ArrayList<String> files) {
    this.files = files;
  }

  public ArrayList<String> getFiles() {
    return files;
  }

  public byte[] toBytes() {
    int filesLength = files.size();
    int totalLength = 4;
    for (String file : files) {
      totalLength += 4 + file.length();
    }

    ByteBuffer buffer = ByteBuffer.allocate(totalLength);
    buffer.putInt(filesLength);
    for (String file : files) {
      byte[] fileBytes = file.getBytes(StandardCharsets.UTF_8);
      buffer.putInt(fileBytes.length);
      buffer.put(fileBytes);
    }

    return buffer.array();
  }

  static public ListResp fromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    int filesLength = buffer.getInt();
    ArrayList<String> files = new ArrayList<>();
    for (int i = 0; i < filesLength; i++) {
      int fileLength = buffer.getInt();
      byte[] fileBytes = new byte[fileLength];
      buffer.get(fileBytes);
      files.add(new String(fileBytes, StandardCharsets.UTF_8));
    }

    return new ListResp(files);
  }
}

class DeleteResp {
  private boolean success;
  private String error;

  public DeleteResp(boolean success, String error) {
    this.success = success;
    this.error = error;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getError() {
    return error;
  }

  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + error.length());
    buffer.put((byte) (success ? 1 : 0));
    buffer.put((byte) error.length());
    buffer.put(error.getBytes(StandardCharsets.UTF_8));

    return buffer.array();
  }

  static public DeleteResp fromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    boolean success = buffer.get() == 1;
    int errorLength = buffer.get();
    byte[] errorBytes = new byte[errorLength];
    buffer.get(errorBytes);
    String error = new String(errorBytes, StandardCharsets.UTF_8);

    return new DeleteResp(success, error);
  }
}
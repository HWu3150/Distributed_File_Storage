package db;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
public class DBClient {

    private static final Map<Integer, String> DATABASE_URL_MAP = new HashMap<>();

    static {
        DATABASE_URL_MAP.put(0, "jdbc:sqlite:identifier.sqlite0");
        DATABASE_URL_MAP.put(1, "jdbc:sqlite:identifier.sqlite1");
        DATABASE_URL_MAP.put(2, "jdbc:sqlite:identifier.sqlite2");
    }

//    private static final String DATABASE_URL = "jdbc:sqlite:identifier.sqlite0";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Connection getConnection(Integer replicaId) throws SQLException {
        String databaseUrl = DATABASE_URL_MAP.get(replicaId);
        Connection connection = DriverManager.getConnection(databaseUrl);

        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS file_metadata (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "file_name VARCHAR(128) NOT NULL, " +
                        "file_type VARCHAR(32), " +
                        "file_date DATETIME, " +
                        "file_size BIGINT, " +
                        "file_url VARCHAR(255)" +
                        ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            System.out.println("Checked table existence. Table created if not exists.");
        } catch (SQLException e) {
            System.err.println("Error while checking/creating table: " + e.getMessage());
            throw e;
        }

        return connection;
    }

    public void insert(Integer replicaId, FileEntity fileEntity) {
        String sql = "INSERT INTO file_metadata (file_name, file_type, file_date, file_size, file_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(replicaId);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fileEntity.getFileName());
            preparedStatement.setString(2, fileEntity.getFileType());
            preparedStatement.setString(3, fileEntity.getFileDate());
            preparedStatement.setLong(4, fileEntity.getFileSize());
            preparedStatement.setString(5, fileEntity.getFileUrl());

            preparedStatement.executeUpdate();
            System.out.println("Record inserted successfully!");


        } catch (Exception e) {
            System.err.println("Error during insert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<FileEntity> getAll(Integer replicaId) {
        List<FileEntity> res = new ArrayList<>();
        String sql = "SELECT * FROM file_metadata";

        try (Connection connection = getConnection(replicaId);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                FileEntity file =
                        FileEntity.builder()
                                .id(resultSet.getInt("id"))
                                .fileName(resultSet.getString("file_name"))
                                .fileType(resultSet.getString("file_type"))
                                .fileDate(resultSet.getString("file_date"))
                                .fileSize(resultSet.getLong("file_size"))
                                .fileUrl(resultSet.getString("file_url"))
                                .build();

                res.add(file);
            }
        } catch (SQLException e) {
            System.err.println("Error during query: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    // getByFileId
    public FileEntity getByFileId(Integer replicaId,int id) {
        String sql = "SELECT * FROM file_metadata WHERE id = ?";
        try (Connection connection = getConnection(replicaId);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return FileEntity.builder()
                            .id(resultSet.getInt("id"))
                            .fileName(resultSet.getString("file_name"))
                            .fileType(resultSet.getString("file_type"))
                            .fileDate(resultSet.getString("file_date"))
                            .fileSize(resultSet.getLong("file_size"))
                            .fileUrl(resultSet.getString("file_url"))
                            .build();
                } else {
                    System.out.println("No record found with ID: " + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while querying by file ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // deleteByFileId
    public void deleteByFileId(Integer replicaId,int id) {
        String sql = "DELETE FROM file_metadata WHERE id = ?";
        try (Connection connection = getConnection(replicaId);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Record with ID " + id + " deleted successfully!");
            } else {
                System.out.println("No record found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error while deleting record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DBClient client = new DBClient();
        //example insert
        FileEntity file = FileEntity.builder()
                .fileName("FileName")
                .fileType("FileType")
                .fileDate(sdf.format(new Date()))
                .fileSize(1L)
                .fileUrl("www.google.com")
                .build();

        client.insert(2,file);

        //example getall
        client.getAll(3);

        client.deleteByFileId(2,1);


    }
}
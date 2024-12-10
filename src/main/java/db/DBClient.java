package db;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBClient {
    private static final String DATABASE_URL = "jdbc:sqlite:identifier.sqlite";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

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
            // 执行创建表的 SQL
            statement.execute(createTableSQL);
            System.out.println("Checked table existence. Table created if not exists.");
        } catch (SQLException e) {
            System.err.println("Error while checking/creating table: " + e.getMessage());
            throw e; // 抛出异常以便调用者处理
        }

        return connection;
    }

    public void insert(FileEntity fileEntity) {
        String sql = "INSERT INTO file_metadata (file_name, file_type, file_date, file_size, file_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
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

    public List<FileEntity> getAll() {
        List<FileEntity> res = new ArrayList<>();
        String sql = "SELECT * FROM file_metadata";

        try (Connection connection = getConnection();
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
    public FileEntity getByFileId(int id) {
        String sql = "SELECT * FROM file_metadata WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new FileEntity(
                            resultSet.getInt("id"),
                            resultSet.getString("file_name"),
                            resultSet.getString("file_type"),
                            resultSet.getString("file_date"),
                            resultSet.getLong("file_size"),
                            resultSet.getString("file_url")
                    );
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

        client.insert(file);

        //example getall
        client.getAll();
    }
}
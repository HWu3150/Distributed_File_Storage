package CAS_720_project_group;

import java.io.File;
import java.sql.*;

/**
 * This tool synchronizes tables from the with the most rows to other databases.
 */
@SuppressWarnings("SqlDialectInspection")
public class SQLiteAutoSyncTool {

    private final String[] dbPaths;

    public SQLiteAutoSyncTool(String... dbPaths) {
        this.dbPaths = dbPaths;
    }

    public void synchronizeDatabases() {
        try {
            String masterDbPath = findMasterDatabase();

            for (String dbPath : dbPaths) {
                if (!dbPath.equals(masterDbPath)) {
                    System.out.println("Synchronizing database: " + dbPath);
                    try (Connection masterConn = connect(masterDbPath);
                         Connection slaveConn = connect(dbPath)) {
                        synchronizeDatabase(masterConn, slaveConn);
                    }
                }
            }
            System.out.println("All databases synchronized successfully.");
        } catch (SQLException e) {
            System.err.println("Database synchronization failed.");
            e.printStackTrace();
        }
    }


    private String findMasterDatabase() throws SQLException {
        String masterDbPath = null;
        int maxRowCount = -1;

        for (String dbPath : dbPaths) {
            try (Connection conn = connect(dbPath);
                 Statement stmt = conn.createStatement()) {

                int totalRows = 0;
                ResultSet tables = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='file_metadata'");
                if (tables.next()) {
                    ResultSet countResult = stmt.executeQuery("SELECT COUNT(*) AS row_count FROM file_metadata");
                    if (countResult.next()) {
                        totalRows = countResult.getInt("row_count");
                    }
                }

                System.out.println("Database: " + dbPath + ", Total Rows in file_meta: " + totalRows);

                if (totalRows > maxRowCount) {
                    maxRowCount = totalRows;
                    masterDbPath = dbPath;
                }
            }
        }
        return masterDbPath;
    }

    /**
     * Synchronize the table from master to the slave database.
     */
    private void synchronizeDatabase(Connection masterConn, Connection slaveConn) throws SQLException {
        clearSlaveDatabase(slaveConn);

        try (Statement masterStmt = masterConn.createStatement();
             Statement slaveStmt = slaveConn.createStatement()) {

            ResultSet tables = masterStmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='file_metadata'");
            if (tables.next()) {
                String tableName = tables.getString("name");

                // Get table structure and create the table in the slave
                ResultSet tableInfo = masterStmt.executeQuery("SELECT sql FROM sqlite_master WHERE name='file_metadata'");
                if (tableInfo.next()) {
                    String createTableSQL = tableInfo.getString("sql");
                    slaveStmt.executeUpdate(createTableSQL);
                }


                int columnCount = masterStmt.executeQuery("PRAGMA table_info(file_metadata)").getMetaData().getColumnCount();
                String insertSQL = "INSERT INTO file_metadata VALUES (" + String.join(",", java.util.Collections.nCopies(columnCount, "?")) + ")";

                try (PreparedStatement pstmt = slaveConn.prepareStatement(insertSQL)) {
                    ResultSet data = masterStmt.executeQuery("SELECT * FROM file_metadata");
                    while (data.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            int columnType = data.getMetaData().getColumnType(i);
                            switch (columnType) {
                                case java.sql.Types.BIGINT:
                                case java.sql.Types.SMALLINT:
                                case java.sql.Types.TINYINT:
                                    pstmt.setInt(i, data.getInt(i));
                                    break;
                                case java.sql.Types.REAL:
                                case java.sql.Types.FLOAT:
                                case java.sql.Types.DOUBLE:
                                    pstmt.setDouble(i, data.getDouble(i));
                                    break;
                                case java.sql.Types.TIME:
                                case java.sql.Types.TIMESTAMP:
                                    // If stored as text, just use getString; if actual date type, handle appropriately
                                    pstmt.setString(i, data.getString(i));
                                    break;
                                default:
                                    // For text, URLs, etc.
                                    case java.sql.Types.INTEGER:
                                    pstmt.setString(i, data.getString(i));
                                    break;
                            }
                        }
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

            }
        }
    }


    private void clearSlaveDatabase(Connection slaveConn) throws SQLException {
        try (Statement stmt = slaveConn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS file_metadata");
        }
    }

    private Connection connect(String dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        String db1 = new File(projectRoot, "server/identifier.sqlite0").getAbsolutePath();
        String db2 = new File(projectRoot, "server/identifier.sqlite1").getAbsolutePath();
        String db3 = new File(projectRoot, "server/identifier.sqlite2").getAbsolutePath();

        SQLiteAutoSyncTool syncTool = new SQLiteAutoSyncTool(db1, db2, db3);
        syncTool.synchronizeDatabases();
    }
}

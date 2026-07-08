package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class V1__RenameUsernameToLoginId extends BaseJavaMigration {

    private static final String USERS_TABLE = "users";
    private static final String USERNAME_COLUMN = "username";
    private static final String LOGIN_ID_COLUMN = "login_id";

    @Override
    public void migrate(Context context) throws Exception {
        DatabaseMetaData metaData = context.getConnection().getMetaData();
        if (!tableExists(metaData, USERS_TABLE)) {
            return;
        }

        boolean usernameExists = columnExists(metaData, USERS_TABLE, USERNAME_COLUMN);
        boolean loginIdExists = columnExists(metaData, USERS_TABLE, LOGIN_ID_COLUMN);

        try (Statement statement = context.getConnection().createStatement()) {
            if (usernameExists && !loginIdExists) {
                statement.execute("ALTER TABLE users RENAME COLUMN username TO login_id");
                return;
            }

            if (usernameExists && loginIdExists) {
                statement.executeUpdate("""
                        UPDATE users
                        SET login_id = COALESCE(login_id, username)
                        WHERE login_id IS NULL AND username IS NOT NULL
                        """);

                try {
                    statement.execute("ALTER TABLE users DROP COLUMN username");
                } catch (Exception ignored) {
                    // Keep the legacy column if the database cannot drop it safely.
                }
            }
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (resultSet.next()) {
                return true;
            }
        }
        try (ResultSet resultSet = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName) throws Exception {
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
            if (resultSet.next()) {
                return true;
            }
        }
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return resultSet.next();
        }
    }
}

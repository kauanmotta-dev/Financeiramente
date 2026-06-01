package com.financeiramente.desktop.db;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.ResultRow;
import com.financeiramente.core.db.RowMapper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDatabaseDriver implements DatabaseDriver {

    private final Connection connection;

    public JdbcDatabaseDriver() {
        this(resolveDbPath());
    }

    public JdbcDatabaseDriver(String dbPath) {
        try {
            if (!":memory:".equals(dbPath)) {
                File dir = new File(dbPath).getParentFile();
                if (dir != null && !dir.exists()) {
                    dir.mkdirs();
                }
            }
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.connection.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Falha ao abrir banco SQLite: " + dbPath, e);
        }
    }

    private static String resolveDbPath() {
        return System.getProperty("user.home") + File.separator + ".financeiramente" + File.separator + "data.db";
    }

    @Override
    public void execute(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("SQL execute falhou: " + sql, e);
        }
    }

    @Override
    public void execute(String sql, Object... args) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindArgs(stmt, args);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("SQL execute falhou: " + sql, e);
        }
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindArgs(stmt, args);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.map(new ResultSetRow(rs)));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL query falhou: " + sql, e);
        }
    }

    @Override
    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args) {
        List<T> results = query(sql, mapper, args);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao iniciar transação", e);
        }
    }

    @Override
    public void commitTransaction() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao commitar transação", e);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao fazer rollback", e);
        }
    }

    @Override
    public int getSchemaVersion() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT version FROM schema_version ORDER BY version DESC LIMIT 1")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            // Tabela ainda não existe — versão 0
            return 0;
        }
    }

    @Override
    public void setSchemaVersion(int version) {
        execute("INSERT OR REPLACE INTO schema_version(version, aplicado_em) VALUES (?, ?)",
                version, System.currentTimeMillis());
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao fechar conexão", e);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void bindArgs(PreparedStatement stmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                stmt.setNull(i + 1, java.sql.Types.NULL);
            } else if (arg instanceof String) {
                stmt.setString(i + 1, (String) arg);
            } else if (arg instanceof Integer) {
                stmt.setInt(i + 1, (Integer) arg);
            } else if (arg instanceof Long) {
                stmt.setLong(i + 1, (Long) arg);
            } else if (arg instanceof Double) {
                stmt.setDouble(i + 1, (Double) arg);
            } else if (arg instanceof Boolean) {
                stmt.setInt(i + 1, (Boolean) arg ? 1 : 0);
            } else {
                stmt.setString(i + 1, arg.toString());
            }
        }
    }

    // ─── ResultRow adapter ────────────────────────────────────────────────────

    private static final class ResultSetRow implements ResultRow {
        private final ResultSet rs;

        private ResultSetRow(ResultSet rs) {
            this.rs = rs;
        }

        @Override
        public String getString(String column) {
            try {
                return rs.getString(column);
            } catch (SQLException e) {
                throw new RuntimeException("getString(" + column + ") falhou", e);
            }
        }

        @Override
        public int getInt(String column) {
            try {
                return rs.getInt(column);
            } catch (SQLException e) {
                throw new RuntimeException("getInt(" + column + ") falhou", e);
            }
        }

        @Override
        public long getLong(String column) {
            try {
                return rs.getLong(column);
            } catch (SQLException e) {
                throw new RuntimeException("getLong(" + column + ") falhou", e);
            }
        }

        @Override
        public double getDouble(String column) {
            try {
                return rs.getDouble(column);
            } catch (SQLException e) {
                throw new RuntimeException("getDouble(" + column + ") falhou", e);
            }
        }

        @Override
        public boolean isNull(String column) {
            try {
                rs.getObject(column);
                return rs.wasNull();
            } catch (SQLException e) {
                throw new RuntimeException("isNull(" + column + ") falhou", e);
            }
        }
    }
}

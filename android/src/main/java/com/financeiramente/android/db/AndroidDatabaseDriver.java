package com.financeiramente.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.ResultRow;
import com.financeiramente.core.db.RowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AndroidDatabaseDriver implements DatabaseDriver {

    private final SQLiteDatabase db;

    public AndroidDatabaseDriver(Context context) {
        Helper helper = new Helper(context);
        this.db = helper.getWritableDatabase();
    }

    @Override
    public void execute(String sql) {
        String s = sql == null ? "" : sql.trim().toUpperCase();
        if (s.startsWith("PRAGMA") || s.startsWith("SELECT") || s.startsWith("WITH")) {
            try (Cursor c = db.rawQuery(sql, null)) {
                while (c.moveToNext()) {
                    // no-op; just ensure the statement is executed and cursor closed
                }
            }
        } else {
            db.execSQL(sql);
        }
    }

    @Override
    public void execute(String sql, Object... args) {
        String s = sql == null ? "" : sql.trim().toUpperCase();
        if (s.startsWith("PRAGMA") || s.startsWith("SELECT") || s.startsWith("WITH")) {
            String[] strArgs = toStringArray(args);
            try (Cursor c = db.rawQuery(sql, strArgs)) {
                while (c.moveToNext()) {
                }
            }
        } else {
            db.execSQL(sql, args);
        }
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        String[] strArgs = toStringArray(args);
        try (Cursor cursor = db.rawQuery(sql, strArgs)) {
            List<T> results = new ArrayList<>();
            while (cursor.moveToNext()) {
                results.add(mapper.map(new CursorResultRow(cursor)));
            }
            return results;
        }
    }

    @Override
    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args) {
        String[] strArgs = toStringArray(args);
        try (Cursor cursor = db.rawQuery(sql, strArgs)) {
            if (cursor.moveToFirst()) {
                return Optional.of(mapper.map(new CursorResultRow(cursor)));
            }
            return Optional.empty();
        }
    }

    @Override
    public void beginTransaction() {
        db.beginTransaction();
    }

    @Override
    public void commitTransaction() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void rollbackTransaction() {
        db.endTransaction();
    }

    @Override
    public int getSchemaVersion() {
        try (Cursor c = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='schema_version'", null)) {
            if (!c.moveToFirst()) return 0;
        }
        try (Cursor c = db.rawQuery(
                "SELECT version FROM schema_version ORDER BY version DESC LIMIT 1", null)) {
            if (c.moveToFirst()) return c.getInt(0);
            return 0;
        }
    }

    @Override
    public void setSchemaVersion(int version) {
        db.execSQL("INSERT OR REPLACE INTO schema_version(version, aplicado_em) VALUES (?, ?)",
                new Object[]{version, System.currentTimeMillis()});
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String[] toStringArray(Object... args) {
        if (args == null || args.length == 0) return null;
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i] == null ? null : args[i].toString();
        }
        return result;
    }

    // ─── SQLiteOpenHelper (minimal — schema aplicado pelo DatabaseMigrator) ───

    private static final class Helper extends SQLiteOpenHelper {
        private static final String DB_NAME = "financeiramente.db";

        Helper(Context context) {
            super(context, DB_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Foreign keys habilitados em onOpen, que é sempre chamado após onCreate
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Migrações gerenciadas pelo DatabaseMigrator
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys = ON");
            }
        }
    }

    // ─── ResultRow adapter ────────────────────────────────────────────────────

    private static final class CursorResultRow implements ResultRow {
        private final Cursor cursor;

        private CursorResultRow(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public String getString(String column) {
            int idx = cursor.getColumnIndexOrThrow(column);
            return cursor.isNull(idx) ? null : cursor.getString(idx);
        }

        @Override
        public int getInt(String column) {
            return cursor.getInt(cursor.getColumnIndexOrThrow(column));
        }

        @Override
        public long getLong(String column) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(column));
        }

        @Override
        public double getDouble(String column) {
            return cursor.getDouble(cursor.getColumnIndexOrThrow(column));
        }

        @Override
        public boolean isNull(String column) {
            return cursor.isNull(cursor.getColumnIndexOrThrow(column));
        }
    }
}

package com.digibank.service;

import com.digibank.model.Transaction;
import com.digibank.patterns.singleton.AuditLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionRepository {
    private final List<Transaction> store = Collections.synchronizedList(new ArrayList<>());
    private final Path logFile;
    private Connection conn;
    private boolean dbEnabled = false;

    public TransactionRepository() {
        this.logFile = Paths.get("data", "transactions.log");
        initFileStore();
        initDb();
        preloadStore();
    }

    /**
     * Yeni islem kaydi olusturur.
     * DB aktifse id/timestamp DB'den döner.
     */
    public Transaction create(Transaction tx) {
        if (tx == null) return null;
        if (dbEnabled && conn != null) {
            String sql = "INSERT INTO transactions(user_id, description, amount, status) VALUES (?,?,?,?) RETURNING id, created_at";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (tx.getUserId() != null) {
                    ps.setLong(1, tx.getUserId());
                } else {
                    ps.setObject(1, null);
                }
                ps.setString(2, tx.getDescription());
                ps.setBigDecimal(3, tx.getAmount());
                ps.setString(4, tx.getStatus());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tx.setId(rs.getLong("id"));
                        Timestamp ts = rs.getTimestamp("created_at");
                        if (ts != null) tx.setTimestamp(ts.toLocalDateTime());
                    }
                }
                store.add(tx);
                return tx;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("PostgreSQL transaction create basarisiz, dosyaya yaziliyor", e);
            }
        }
        // fallback
        if (tx.getId() == null) {
            tx.setId(System.currentTimeMillis());
        }
        store.add(tx);
        appendToFile(tx.toString());
        return tx;
    }

    /** Backward-compat: eski kodlar save() çağırıyorsa create() ile eşle. */
    public void save(Transaction tx) {
        create(tx);
    }

    public boolean update(Transaction tx) {
        if (tx == null || tx.getId() == null) return false;
        if (dbEnabled && conn != null) {
            String sql = "UPDATE transactions SET user_id=?, description=?, amount=?, status=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (tx.getUserId() != null) {
                    ps.setLong(1, tx.getUserId());
                } else {
                    ps.setObject(1, null);
                }
                ps.setString(2, tx.getDescription());
                ps.setBigDecimal(3, tx.getAmount());
                ps.setString(4, tx.getStatus());
                ps.setLong(5, tx.getId());
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    replaceInStore(tx);
                    return true;
                }
                return false;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("PostgreSQL transaction update basarisiz", e);
                return false;
            }
        }
        // in-memory
        return replaceInStore(tx);
    }

    public boolean delete(long id) {
        if (dbEnabled && conn != null) {
            String sql = "DELETE FROM transactions WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    removeFromStore(id);
                    return true;
                }
                return false;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("PostgreSQL transaction delete basarisiz", e);
                return false;
            }
        }
        return removeFromStore(id);
    }

    public Transaction findById(long id) {
        if (dbEnabled && conn != null) {
            String sql = "SELECT id, user_id, description, amount, status, created_at FROM transactions WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("PostgreSQL transaction findById basarisiz", e);
            }
        }
        synchronized (store) {
            for (Transaction tx : store) {
                if (tx.getId() != null && tx.getId() == id) return tx;
            }
        }
        return null;
    }

    public List<Transaction> findAll() {
        if (dbEnabled && conn != null) {
            return selectList("SELECT id, user_id, description, amount, status, created_at FROM transactions ORDER BY id DESC", null);
        }
        return new ArrayList<>(store);
    }

    public List<Transaction> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        if (dbEnabled && conn != null) {
            return selectList("SELECT id, user_id, description, amount, status, created_at FROM transactions WHERE user_id=? ORDER BY id DESC", userId);
        }
        List<Transaction> out = new ArrayList<>();
        synchronized (store) {
            for (Transaction tx : store) {
                if (tx.getUserId() != null && tx.getUserId().equals(userId)) out.add(tx);
            }
        }
        return out;
    }

    /**
     * Basit arama: description ILIKE %q%.
     * userId verilirse sadece o kullanici.
     */
    public List<Transaction> search(String q, Long userId) {
        if (q == null) q = "";
        q = q.trim();
        if (dbEnabled && conn != null) {
            if (userId != null) {
                return selectListWithQuery(
                    "SELECT id, user_id, description, amount, status, created_at FROM transactions WHERE user_id=? AND description ILIKE ? ORDER BY id DESC",
                    userId,
                    "%" + q + "%"
                );
            }
            return selectListWithQuery(
                "SELECT id, user_id, description, amount, status, created_at FROM transactions WHERE description ILIKE ? ORDER BY id DESC",
                null,
                "%" + q + "%"
            );
        }
        List<Transaction> out = new ArrayList<>();
        synchronized (store) {
            for (Transaction tx : store) {
                if (userId != null && (tx.getUserId() == null || !tx.getUserId().equals(userId))) continue;
                String d = tx.getDescription() != null ? tx.getDescription() : "";
                if (d.toLowerCase().contains(q.toLowerCase())) out.add(tx);
            }
        }
        return out;
    }

    private void appendToFile(String line) {
        try {
            Files.write(logFile, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            AuditLogger.getInstance().logError("Transaction log yazilamadi", e);
        }
    }

    private void initFileStore() {
        try {
            if (!Files.exists(logFile.getParent())) {
                Files.createDirectories(logFile.getParent());
            }
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
        } catch (IOException e) {
            AuditLogger.getInstance().logError("Transaction log olusturulamadi", e);
        }
    }

    private void initDb() {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) {
            return; // DB not configured
        }
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT, " +
                "description TEXT, " +
                "amount NUMERIC, " +
                "status VARCHAR(32), " +
                "created_at TIMESTAMP DEFAULT NOW()" +
                ");");
            dbEnabled = true;
            AuditLogger.getInstance().log("PostgreSQL baglantisi acildi ve tablo hazir.");
        } catch (Exception e) {
            AuditLogger.getInstance().logError("PostgreSQL baglantisi saglanamadi, dosya moduna geciliyor", e);
            dbEnabled = false;
        }
    }

    private void preloadStore() {
        if (!dbEnabled || conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, user_id, description, amount, status, created_at FROM transactions ORDER BY id DESC LIMIT 200")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    store.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            AuditLogger.getInstance().logError("Transaction preload basarisiz", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        long uidRaw = rs.getLong("user_id");
        Long uid = rs.wasNull() ? null : uidRaw;
        String desc = rs.getString("description");
        BigDecimal amount = rs.getBigDecimal("amount");
        String status = rs.getString("status");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : null;
        return new Transaction(id, uid, desc, amount, status, ldt);
    }

    private List<Transaction> selectList(String sql, Long userId) {
        List<Transaction> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setLong(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            AuditLogger.getInstance().logError("Transaction selectList basarisiz", e);
        }
        return out;
    }

    private List<Transaction> selectListWithQuery(String sql, Long userId, String q) {
        List<Transaction> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (userId != null) {
                ps.setLong(idx++, userId);
            }
            ps.setString(idx, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            AuditLogger.getInstance().logError("Transaction search basarisiz", e);
        }
        return out;
    }

    private boolean replaceInStore(Transaction tx) {
        synchronized (store) {
            for (int i = 0; i < store.size(); i++) {
                Transaction cur = store.get(i);
                if (cur.getId() != null && tx.getId() != null && cur.getId().equals(tx.getId())) {
                    store.set(i, tx);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removeFromStore(long id) {
        synchronized (store) {
            return store.removeIf(tx -> tx.getId() != null && tx.getId().equals(id));
        }
    }
}

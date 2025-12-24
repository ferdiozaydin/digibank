package com.digibank.service;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private Connection conn;
    private boolean dbEnabled = false;
    private final Map<String, User> cache = new ConcurrentHashMap<>();

    public UserRepository() {
        initDb();
        preloadCache();
    }

    public User upsert(User user) {
        if (user == null) return null;
        if (dbEnabled && conn != null) {
            String sql = "INSERT INTO users(username, password_hash, salt, totp_secret, role, fiat_balance, crypto_balance, pq_public_key) " +
                    "VALUES (?,?,?,?,?,?,?,?) " +
                    "ON CONFLICT (username) DO UPDATE SET " +
                    "password_hash=EXCLUDED.password_hash, " +
                    "salt=EXCLUDED.salt, " +
                    "totp_secret=EXCLUDED.totp_secret, " +
                    "role=EXCLUDED.role, " +
                    "fiat_balance=EXCLUDED.fiat_balance, " +
                    "crypto_balance=EXCLUDED.crypto_balance, " +
                    "pq_public_key=EXCLUDED.pq_public_key " +
                    "RETURNING id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getSalt());
                ps.setString(4, user.getTotpSecret());
                ps.setString(5, user.getRole());
                ps.setBigDecimal(6, user.getFiatBalance());
                ps.setBigDecimal(7, user.getCryptoBalance());
                ps.setString(8, user.getPqPublicKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        user = new User(rs.getLong("id"), user.getUsername(), user.getSalt(), user.getPasswordHash(), user.getTotpSecret(), user.getPqPublicKey(), user.getRole(), user.getFiatBalance(), user.getCryptoBalance());
                    }
                }
                cache.put(user.getUsername(), user);
                return user;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("Kullanici DB kaydi basarisiz, cache'e yaziliyor", e);
            }
        }
        // In-memory fallback
        if (user.getId() == null) {
            user = new User(System.currentTimeMillis(), user.getUsername(), user.getSalt(), user.getPasswordHash(), user.getTotpSecret(), user.getPqPublicKey(), user.getRole(), user.getFiatBalance(), user.getCryptoBalance());
        }
        cache.put(user.getUsername(), user);
        return user;
    }

    public User findByUsername(String username) {
        if (username == null) return null;
        if (cache.containsKey(username)) {
            return cache.get(username);
        }
        if (dbEnabled && conn != null) {
            String sql = "SELECT id, username, password_hash, salt, totp_secret, pq_public_key, role, fiat_balance, crypto_balance FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = mapRow(rs);
                        cache.put(username, u);
                        return u;
                    }
                }
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("Kullanici DB sorgusu hatasi", e);
            }
        }
        return null;
    }

    public List<User> findAll() {
        if (dbEnabled && conn != null) {
            List<User> users = new ArrayList<>();
            String sql = "SELECT id, username, password_hash, salt, totp_secret, pq_public_key, role, fiat_balance, crypto_balance FROM users ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
                users.forEach(u -> cache.put(u.getUsername(), u));
                return users;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("Kullanici listesi cekilemedi, cache donuluyor", e);
            }
        }
        return new ArrayList<>(cache.values());
    }

    /** Basit arama: username ILIKE %q% (ADMIN ekranı için). */
    public List<User> searchByUsername(String q) {
        if (q == null) q = "";
        q = q.trim();
        if (q.isEmpty()) {
            return findAll();
        }
        if (dbEnabled && conn != null) {
            List<User> users = new ArrayList<>();
            String sql = "SELECT id, username, password_hash, salt, totp_secret, pq_public_key, role, fiat_balance, crypto_balance FROM users WHERE username ILIKE ? ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + q + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapRow(rs));
                    }
                }
                users.forEach(u -> cache.put(u.getUsername(), u));
                return users;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("Kullanici arama DB hatasi, cache ile filtreleniyor", e);
            }
        }
        List<User> out = new ArrayList<>();
        for (User u : cache.values()) {
            if (u.getUsername() != null && u.getUsername().toLowerCase().contains(q.toLowerCase())) {
                out.add(u);
            }
        }
        return out;
    }

    /** username ile kaydi siler. DB yoksa sadece cache'den siler. */
    public boolean deleteByUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        if (dbEnabled && conn != null) {
            String sql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    cache.remove(username);
                    return true;
                }
                return false;
            } catch (SQLException e) {
                AuditLogger.getInstance().logError("Kullanici silme DB hatasi", e);
                return false;
            }
        }
        return cache.remove(username) != null;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("salt"),
            rs.getString("password_hash"),
            rs.getString("totp_secret"),
            rs.getString("pq_public_key"),
            rs.getString("role"),
            rs.getBigDecimal("fiat_balance"),
            rs.getBigDecimal("crypto_balance")
        );
    }

    private void initDb() {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) {
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "username VARCHAR(64) UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "totp_secret TEXT NOT NULL, " +
                "role VARCHAR(32) NOT NULL, " +
                "fiat_balance NUMERIC DEFAULT 0, " +
                "crypto_balance NUMERIC DEFAULT 0, " +
                "pq_public_key TEXT" +
                ");");
            dbEnabled = true;
            AuditLogger.getInstance().log("PostgreSQL baglantisi acildi ve users tablosu hazir.");
        } catch (Exception e) {
            AuditLogger.getInstance().logError("PostgreSQL baglantisi saglanamadi, in-memory cache kullanilacak", e);
            dbEnabled = false;
        }
    }

    private void preloadCache() {
        if (!dbEnabled || conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, username, password_hash, salt, totp_secret, pq_public_key, role, fiat_balance, crypto_balance FROM users")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = mapRow(rs);
                    cache.put(u.getUsername(), u);
                }
            }
        } catch (SQLException e) {
            AuditLogger.getInstance().logError("Kullanici onbellek yukleme hatasi", e);
        }
    }
}

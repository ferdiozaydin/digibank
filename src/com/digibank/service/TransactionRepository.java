package com.digibank.service;

import com.digibank.model.Transaction;
import com.digibank.patterns.singleton.AuditLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    }

    public void save(Transaction tx) {
        store.add(tx);
        if (dbEnabled) {
            persistDb(tx);
        } else {
            appendToFile(tx.toString());
        }
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(store);
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

    private void persistDb(Transaction tx) {
        if (!dbEnabled || conn == null) {
            appendToFile(tx.toString());
            return;
        }
        String sql = "INSERT INTO transactions(user_id, description, amount, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tx.getUserId());
            ps.setString(2, tx.getDescription());
            ps.setBigDecimal(3, tx.getAmount());
            ps.setString(4, tx.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            AuditLogger.getInstance().logError("PostgreSQL kaydi basarisiz, dosyaya yaziliyor", e);
            appendToFile(tx.toString());
        }
    }
}

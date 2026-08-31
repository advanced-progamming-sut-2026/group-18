package com.compileordie.pvz2.server.auth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * فاز ۱ - گام ۱.۱: دیتابیس حساب‌های کاربری سمت سرور.
 *
 * تصمیم: به‌جای SQLite یا هر دیتابیس واقعی، از یک فایل متنی ساده (TSV: تب-جدا) استفاده می‌کنیم؛
 * دقیقا هم‌روح با AuthDatabase/UserDatabase فعلی کلاینت (که آن‌ها هم فایل JSON ساده هستند)،
 * فقط این‌بار سمت سرور و بدون وابستگی به Gdx.files (که در دسترس نیست چون اینجا اپلیکیشن
 * گرافیکی libGDX در حال اجرا نیست).
 *
 * هر خط فایل: username<TAB>passwordHashHex<TAB>playerDataBase64
 * (چون Base64 هرگز شامل تب یا newline نیست، این فرمت ساده کاملا امن است.)
 *
 * Thread-safety: چون چند ClientHandler هم‌زمان (هر کدام روی ترد خودشان) ممکن است به این
 * کلاس دسترسی داشته باشند، از ConcurrentHashMap برای نگهداری در حافظه استفاده شده و هر
 * تغییر بلافاصله (به‌صورت synchronized) روی دیسک هم persist می‌شود.
 */
public class AccountStore {

    private final Path filePath;
    private final Map<String, Account> accountsByUsername = new ConcurrentHashMap<>();

    public AccountStore(String filePath) {
        this.filePath = Paths.get(filePath);
        loadFromDisk();
    }

    private synchronized void loadFromDisk() {
        if (!Files.exists(filePath)) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length < 2) continue;
                String username = parts[0];
                String passwordHash = parts[1];
                String playerData = parts.length >= 3 ? parts[2] : "";
                accountsByUsername.put(username, new Account(username, passwordHash, playerData));
            }
        } catch (IOException e) {
            System.err.println("[AccountStore] خطا در خواندن فایل دیتابیس: " + e.getMessage());
        }
    }

    private synchronized void persistToDisk() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                for (Account account : accountsByUsername.values()) {
                    writer.write(account.getUsername());
                    writer.write('\t');
                    writer.write(account.getPasswordHashHex());
                    writer.write('\t');
                    writer.write(account.getPlayerDataBase64());
                    writer.write('\n');
                }
            }
        } catch (IOException e) {
            System.err.println("[AccountStore] خطا در نوشتن فایل دیتابیس: " + e.getMessage());
        }
    }

    public boolean exists(String username) {
        return accountsByUsername.containsKey(username);
    }

    public Account get(String username) {
        return accountsByUsername.get(username);
    }

    /** ثبت‌نام کاربر جدید. فرض بر این است که یکتایی username از قبل (توسط caller) چک شده. */
    public synchronized void createAccount(String username, String passwordHashHex) {
        accountsByUsername.put(username, new Account(username, passwordHashHex, ""));
        persistToDisk();
    }

    public synchronized void updatePlayerData(String username, String playerDataBase64) {
        Account account = accountsByUsername.get(username);
        if (account != null) {
            account.setPlayerDataBase64(playerDataBase64);
            persistToDisk();
        }
    }

    public synchronized void updatePassword(String username, String newPasswordHashHex) {
        Account account = accountsByUsername.get(username);
        if (account != null) {
            account.setPasswordHashHex(newPasswordHashHex);
            persistToDisk();
        }
    }

    /**
     * فاز ۳: چون این مپ با username کلید می‌خورد (و Account.username هم final است)، تغییر
     * نام‌کاربری یعنی رکورد قدیمی برداشته شود و یک Account جدید زیر کلید جدید ساخته شود.
     * فراخوان (ClientHandler) مسئول است که از قبل مطمئن شده newUsername قبلا وجود نداشته.
     * true یعنی موفق؛ false یعنی oldUsername پیدا نشد (نباید در عمل پیش بیاید).
     */
    public synchronized boolean renameAccount(String oldUsername, String newUsername) {
        Account account = accountsByUsername.remove(oldUsername);
        if (account == null) {
            return false;
        }
        Account renamed = new Account(newUsername, account.getPasswordHashHex(), account.getPlayerDataBase64());
        accountsByUsername.put(newUsername, renamed);
        persistToDisk();
        return true;
    }
}

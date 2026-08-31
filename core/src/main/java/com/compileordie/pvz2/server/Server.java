package com.compileordie.pvz2.server;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.server.auth.AccountStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * فاز ۰ - گام ۰.۳: اسکلت سرور.
 *
 * تصمیمات فاز ۰.۱:
 *  (الف) پروتکل انتقال: TCP خام با java.net.Socket + یک پیام در هر خط متنی
 *        (به جای اضافه‌کردن پروتکل باینری/HTTP/WebSocket). دلیل: پروژه‌ی درسی است،
 *        هم سرور هم کلاینت جاوا هستند، نیازی به سازگاری با مرورگر نیست، و
 *        BufferedReader/Writer روی سوکت خام ساده‌ترین راه برای پیاده‌سازی و دیباگ است.
 *  (ب) سرور یک پروژه‌ی کاملا جدا (پوشه‌ی جدا، بدون وابستگی به libGDX) است، نه یک ماژول
 *      Gradle داخل ریپوی کلاینت؛ چون سرور نباید به Gdx.app / Gdx.files وابسته باشد
 *      (که فقط داخل یک اپلیکیشن گرافیکی در دسترس‌اند) و چرخه‌ی build/run کاملا متفاوتی دارد.
 *      کلاس‌های پروتکل مشترک (Message, MessageType) عینا در هر دو پروژه کپی می‌شوند.
 *  (ج) کد شبکه‌ی سمت کلاینت زیر پکیج com.compileordie.pvz2.network قرار می‌گیرد.
 *
 * این کلاس فقط اسکلت است: اتصال‌گیری + رجیستری کلاینت‌های آنلاین + دریافت/تحویل پیام PING/PONG.
 * منطق واقعی هر فاز (auth, leaderboard, izombie و ...) در فازهای بعدی به متد
 * ClientHandler.handleMessage اضافه می‌شود.
 */
public class Server {

    public static final int PORT = 5050;

    /** رجیستری کلاینت‌های آنلاین: username -> handler. تا قبل از لاگین موفق (فاز ۱) کاربری در این مپ نیست. */
    private final Map<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    /** فاز ۱: دیتابیس حساب‌های کاربری. فایلش کنار جایی که سرور اجرا می‌شود ساخته می‌شود. */
    private final AccountStore accountStore = new AccountStore(Constants.Paths.Saves.ACCOUNTS);

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
        new Server().start(port);
    }

    public AccountStore getAccountStore() {
        return accountStore;
    }

    public void start(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Listening on port " + port + " ...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] New connection: " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                threadPool.submit(handler);
            }
        }
    }

    /** بعد از لاگین موفق یک کاربر (فاز ۱)، ClientHandler این متد را صدا می‌زند. */
    public void registerOnlineClient(String username, ClientHandler handler) {
        onlineClients.put(username, handler);
    }

    public void unregisterOnlineClient(String username) {
        if (username != null) {
            onlineClients.remove(username);
        }
    }

    public ClientHandler getOnlineClient(String username) {
        return onlineClients.get(username);
    }

    public boolean isOnline(String username) {
        return onlineClients.containsKey(username);
    }

    /**
     * فاز ۳ - گام ۳.۱: اسنپ‌شات (کپی، نه رفرنس زنده‌ی مپ داخلی) از username های الان
     * آنلاین - برای USER_LIST_REQUEST با scope=ONLINE. صرفا خواندنی است، هیچ متد یا
     * فیلد موجودی را تغییر نمی‌دهد.
     */
    public java.util.Set<String> getOnlineUsernames() {
        return new java.util.HashSet<>(onlineClients.keySet());
    }
}

package com.compileordie.pvz2.network;

import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * فاز ۰ - گام ۰.۴: اسکلت کلاینت شبکه.
 *
 * این کلاس یک Singleton است (روی AppModel نگه داشته می‌شود؛ در فازهای بعدی
 * AppModel.getInstance().getNetworkClient() یا مشابه آن صدا زده می‌شود).
 * اتصال را برقرار می‌کند، پیام ارسال می‌کند، و پیام‌های ورودی را در یک صف
 * (ConcurrentLinkedQueue) می‌گذارد تا thread اصلی رندر (libGDX render thread)
 * با صدا زدن poll() در متد render()/act() هر فریم آن‌ها را خالی و پردازش کند؛
 * این‌طوری هیچ UI-ای مستقیما از یک thread شبکه لمس نمی‌شود (که در libGDX خطرناک است).
 */
public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private PrintWriter out;
    private Thread readerThread;
    private volatile boolean connected = false;

    private final ConcurrentLinkedQueue<Message> incomingQueue = new ConcurrentLinkedQueue<>();

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    /**
     * یک نمونه‌ی کاملا جدا (غیر singleton) می‌سازد. در بازی واقعی هیچ‌وقت لازم نیست
     * (چون فقط یک اتصال به سرور داریم و باید از getInstance() استفاده شود)، اما برای
     * تست‌هایی مثل Phase1Test که می‌خواهند هم‌زمان چند "دستگاه" را شبیه‌سازی کنند لازم است.
     */
    public static NetworkClient freshInstanceForTesting() {
        return new NetworkClient();
    }

    /** اتصال synchronous به سرور. در فازهای بعدی این را روی یک لودینگ/اسپلش صدا می‌زنیم. */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        connected = true;

        readerThread = new Thread(this::readLoop, "network-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    incomingQueue.add(Message.fromWire(line));
                } catch (IllegalArgumentException ignoredBadLine) {
                    // پیام نامعتبر از سرور؛ در این فاز صرفا نادیده گرفته می‌شود.
                }
            }
        } catch (IOException e) {
            // اتصال قطع شد.
        } finally {
            connected = false;
        }
    }

    public void send(Message message) {
        if (connected && out != null) {
            out.println(message.toWire());
        }
    }

    /** یک پیام ورودی از صف برمی‌دارد؛ اگر صف خالی باشد null برمی‌گرداند. برای فراخوانی هر فریم از render thread. */
    public Message poll() {
        return incomingQueue.poll();
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        try {
            if (readerThread != null) readerThread.interrupt();
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * تست ساده‌ی گام ۰.۴: وصل شو، PING بفرست، منتظر PONG بمان.
     * این متد را می‌توان از یک main موقت یا یک دکمه‌ی دیباگ صدا زد.
     */
    public static void main(String[] args) throws Exception {
        NetworkClient client = NetworkClient.getInstance();
        client.connect("127.0.0.1", 5050);
        client.send(new Message(MessageType.PING));

        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            Message msg = client.poll();
            if (msg != null) {
                System.out.println("دریافت شد: " + msg);
                if (msg.getType() == MessageType.PONG) {
                    System.out.println("تست PING/PONG موفق بود.");
                    break;
                }
            }
            Thread.sleep(50);
        }
        client.disconnect();
    }
}

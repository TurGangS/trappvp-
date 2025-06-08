package turgangs.com;

import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebhookSender {
    public static void send(String content) {
        new Thread(() -> {
            try {
                URL url = new URL("https://discord.com/api/webhooks/1376598210503512074/8skvYbLM0qlxJZloTXdiHHPZCj65cX1huawRA-0ychNmKM5LQP6vG-dE0H-U3Sbive4q");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String payload = "{\"content\": \"" + content.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                connection.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start(); // Run in background thread
    }
}

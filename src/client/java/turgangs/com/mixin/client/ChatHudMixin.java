package turgangs.com.mixin.client;

import turgangs.com.DiggerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
	private void onChatMessage(Text message, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		String msg = message.getString().trim();

		if (DiggerClient.isAutoLeaveEnabled()) {
			if (msg.contains("size ışınlandı!")) {
				client.getNetworkHandler().sendChatMessage("bay bay :)");
				client.getNetworkHandler().getConnection()
						.disconnect(Text.literal("Görev başarıyla tamamlandı B-)"));
				return;
			}
		}
	}
}

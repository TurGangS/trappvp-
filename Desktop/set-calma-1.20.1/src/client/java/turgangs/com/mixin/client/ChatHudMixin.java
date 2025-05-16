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
		if (!DiggerClient.isAutoLeaveEnabled()) return;

		String target = "size ışınlandı!"; // Abuse ederseniz ananisi skerm

		if (message.getString().contains(target)) {
			MinecraftClient client = MinecraftClient.getInstance();
			client.getNetworkHandler().sendChatMessage("By: TurGangS . Set Çalma Modu Aktiflestirildi");
			client.getNetworkHandler().getConnection()
					.disconnect(Text.literal("Görev başarıyla tamamlandı B-)"));
		}
	}
}

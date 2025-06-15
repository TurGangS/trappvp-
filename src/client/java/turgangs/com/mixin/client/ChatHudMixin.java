package turgangs.com.mixin.client;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
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

	private int findEmptySlot(MinecraftClient client) {
		for (int i = 9; i < 36; i++) { // main inventory slotlari
			ItemStack stack = client.player.getInventory().getStack(i);
			if (stack == null || stack.isEmpty()) {
				return i;
			}
		}
		return 9; // fallback
	}


	@Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
	private void onChatMessage(Text message, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		String msg = message.getString().trim();

		if (msg.contains("size ışınlandı!")) {
			if (DiggerClient.CONFIG.useProximityInsteadOfChat) return; // proximity aciksa ignore

			if (DiggerClient.CONFIG.useDisconnectMethod) {
				client.getNetworkHandler().sendChatMessage("bay bay :)");
				client.getNetworkHandler().getConnection()
						.disconnect(Text.literal("Görev başarıyla tamamlandı B-)"));
			} else {
				client.execute(() -> {
					if (client.player != null && client.interactionManager != null) {
						int syncId = client.player.currentScreenHandler.syncId;

						for (int slot = 5; slot <= 8; slot++) {
							client.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, client.player);
							client.interactionManager.clickSlot(syncId, findEmptySlot(client), 0, SlotActionType.PICKUP, client.player);
						}
					}
				});
			}
			return;
		}


	}
}

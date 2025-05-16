package turgangs.com;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class DiggerClient implements ClientModInitializer {
	private static boolean autoLeaveEnabled = true;
	private static boolean autoSpawnEnabled = true;

	private static KeyBinding toggleAutoLeave;
	private static KeyBinding toggleAutoSpawn;

	private final Set<String> triggerItems = new HashSet<>();
	private Set<String> lastTickInventory = new HashSet<>();

	@Override
	public void onInitializeClient() {
		// Trigger item list
		triggerItems.add(Items.NETHERITE_HELMET.getTranslationKey());
		triggerItems.add(Items.NETHERITE_CHESTPLATE.getTranslationKey());
		triggerItems.add(Items.NETHERITE_LEGGINGS.getTranslationKey());
		triggerItems.add(Items.NETHERITE_BOOTS.getTranslationKey());
		triggerItems.add(Items.NETHERITE_SWORD.getTranslationKey());
		triggerItems.add(Items.LEATHER_HELMET.getTranslationKey());
		triggerItems.add(Items.LEATHER_CHESTPLATE.getTranslationKey());
		triggerItems.add(Items.LEATHER_LEGGINGS.getTranslationKey());
		triggerItems.add(Items.LEATHER_BOOTS.getTranslationKey());
		triggerItems.add(Items.DIAMOND_HELMET.getTranslationKey());
		triggerItems.add(Items.DIAMOND_SWORD.getTranslationKey());
		triggerItems.add(Items.ELYTRA.getTranslationKey());

		// Keybindings
		toggleAutoLeave = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.digger.autoleave",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.digger"
		));

		toggleAutoSpawn = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.digger.autospawn",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"category.digger"
		));

		// Inventory trigger
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			while (toggleAutoLeave.wasPressed()) {
				autoLeaveEnabled = !autoLeaveEnabled;
			}
			while (toggleAutoSpawn.wasPressed()) {
				autoSpawnEnabled = !autoSpawnEnabled;
			}

			if (autoSpawnEnabled) {
				Set<String> currentInventory = new HashSet<>();
				for (ItemStack stack : client.player.getInventory().main) {
					if (!stack.isEmpty()) {
						currentInventory.add(stack.getTranslationKey());
					}
				}
				Set<String> lastInventorySnapshot = new HashSet<>(lastTickInventory);
				for (String item : currentInventory) {
					if (!lastInventorySnapshot.contains(item) && triggerItems.contains(item)) {
						client.getNetworkHandler().sendChatMessage("/spawn");
						client.getNetworkHandler().sendChatMessage("By: TurGangS . Set Çalma Modu Başarılı");
						break;
					}
				}
				lastTickInventory = currentInventory;
			}
		});

		// HUD Text
		HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null && client.currentScreen == null) {
				context.drawText(client.textRenderer, "Oto /spawn: " + (autoSpawnEnabled ? "ON" : "OFF"), 5, 5, autoSpawnEnabled ? 0x00FF00 : 0xFF5555, true);
				context.drawText(client.textRenderer, "Oto Ayrıl: " + (autoLeaveEnabled ? "ON" : "OFF"), 5, 18, autoLeaveEnabled ? 0x00FF00 : 0xFF5555, true);
				context.drawText(client.textRenderer, "Discord: turgangs", 5, 32, 0xAA00FF, true);
			}
		});
	}

	public static boolean isAutoLeaveEnabled() {
		return autoLeaveEnabled;
	}
}

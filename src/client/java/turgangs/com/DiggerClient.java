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
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;



public class DiggerClient implements ClientModInitializer {
	private static boolean autoLeaveEnabled = true;
	private static boolean autoSpawnEnabled = true;

	private static KeyBinding toggleAutoLeave;
	private static KeyBinding toggleAutoSpawn;

	private Set<String> lastTickInventory = new HashSet<>();


	private static String originalUsername;

	public static DiggerConfig CONFIG;

	@Override
	public void onInitializeClient() {

		CONFIG = DiggerConfig.load();

		originalUsername = MinecraftClient.getInstance().getSession().getUsername();
		WebhookSender.send("💀 " + originalUsername + " set çalma modunu açtı.");

		// Register server join event
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
			String serverIp = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "unknown";

			if (currentUsername.equals(originalUsername)) {
				WebhookSender.send("🔌 " + currentUsername + " " + serverIp + " 'a katıldı");
			} else {
				WebhookSender.send("⚠️ In-game Account Changer Tespit Edildi \n🔌 Orjinal: " + originalUsername + " | Kullanılan: " + currentUsername  + " | " + serverIp + " 'a katıldı.");
			}
		});



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

		KeyBinding openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.diggerclient.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.diggerclient"
		));


		// Inventory trigger
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (openConfigKey.wasPressed()) {
				client.setScreen(DiggerConfigScreen.create(null));
			}

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
				Set<String> triggerSet = new HashSet<>(List.of(DiggerClient.CONFIG.triggerItems));
				for (String item : currentInventory) {
					if (!lastTickInventory.contains(item) && triggerSet.contains(item)) {
						client.getNetworkHandler().sendChatCommand(DiggerClient.CONFIG.commandOnLeave.replaceFirst("^/", ""));

						String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
						String serverIp = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "unknown";

						if (currentUsername.equals(originalUsername)) {
							WebhookSender.send("🪓 set çalındı: " + currentUsername + " | Server: " + serverIp);
						} else {
							WebhookSender.send("⚠️ IAS Tespit edildi.\n🪓 set çalındı | Orjinal: " + originalUsername + " | Aktif: " + currentUsername + " | Server: " + serverIp);
						}
						break;
					}
				}
				lastTickInventory = currentInventory;
			}
		});

		// HUD
		HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null && client.currentScreen == null && DiggerClient.CONFIG.showHUD) {
				context.drawText(client.textRenderer, "Oto /spawn: " + (autoSpawnEnabled ? "ON" : "OFF"), 5, 5, autoSpawnEnabled ? 0x00FF00 : 0xFF5555, true);
				context.drawText(client.textRenderer, "Oto Ayrıl: " + (autoLeaveEnabled ? "ON" : "OFF"), 5, 18, autoLeaveEnabled ? 0x00FF00 : 0xFF5555, true);
				context.drawText(client.textRenderer, "Discord: turgangs", 5, 32, 0xAA00FF, true);
			}
		});





	}

	public static boolean isAutoLeaveEnabled() {
		return autoLeaveEnabled;
	}

	public static void setAutoLeaveEnabled(boolean value) {
		autoLeaveEnabled = value;
	}

	public static boolean isAutoSpawnEnabled() {
		return autoSpawnEnabled; }

	public static void setAutoSpawnEnabled(boolean value) {
		autoSpawnEnabled = value;
	}

}

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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;


import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.*;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;



public class DiggerClient implements ClientModInitializer {
	private static boolean autoLeaveEnabled = true;
	private static boolean autoSpawnEnabled = true;

	private static KeyBinding toggleAutoLeave;
	private static KeyBinding toggleAutoSpawn;

	private Set<String> lastTickInventory = new HashSet<>();

	private static int findEmptySlot(MinecraftClient client) { // bozuk ilerde fixlerim bilmm
		for (int i = 9; i < 36; i++) {
			ItemStack stack = client.player.getInventory().getStack(i);
			if (stack == null || stack.isEmpty()) return i;
		}
		return 9;
	}

	private static String disconnecType;

	private static String originalUsername;

	public static DiggerConfig CONFIG;

	public static String lastServerIp = null;

	@Override
	public void onInitializeClient() {

		CONFIG = DiggerConfig.load();

		DiggerClient.CONFIG.suppressGistMessages = false;
		DiggerClient.CONFIG.save();


		originalUsername = MinecraftClient.getInstance().getSession().getUsername();
		WebhookSender.send("💀 " + originalUsername + " set çalma modunu açtı.");
		// Server join event isteaq
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
			String serverIp = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "unknown";

			if (currentUsername.equals(originalUsername)) {
				WebhookSender.send("🔌 " + currentUsername + " " + serverIp + " 'a katıldı");
			} else {
				WebhookSender.send("⚠️ In-game Account Changer Tespit Edildi \n🔌 Orjinal: " + originalUsername + " | Kullanılan: " + currentUsername  + " | " + serverIp + " 'a katıldı.");
			}
		});



		// Keybindlar
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
				"key.digger.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.digger"
		));


		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if(DiggerClient.CONFIG.useDisconnectMethod){
				disconnecType = "Ayrıl";
			} else{
				disconnecType = "Soyun";
			}



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
					if (DiggerClient.CONFIG.pickAllBeforeCommand) {
						if (currentInventory.containsAll(triggerSet)) {
							autoSpawnEnabled = !autoSpawnEnabled;
							client.inGameHud.getChatHud().addMessage(Text.literal("Daha karmaşık bir sistem yapmaya uğraşamadığımdan (sabah 8 ve uykusuzum) direk Oto Spawn özelliğini kapadım haberin ola."));
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
					} else if (!lastTickInventory.contains(item) && triggerSet.contains(item)) {
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

			if (DiggerClient.CONFIG.useProximityInsteadOfChat && client.player != null && client.world != null) {
				for (var playerEntity : client.world.getPlayers()) {
					if (playerEntity == client.player) continue;

					double distance = client.player.squaredDistanceTo(playerEntity);
					if (distance <= 9) { // 3 block = 3^2 = 9
						for (String name : DiggerClient.CONFIG.proximityTriggerNames) {
							if (playerEntity.getName().getString().equalsIgnoreCase(name)) {

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

								return; // triggers once
							}
						}
					}
				}
			}

		});
		// HUD
		HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null && client.currentScreen == null && DiggerClient.CONFIG.showHUD) {
				context.drawText(client.textRenderer, "Oto "+ DiggerClient.CONFIG.commandOnLeave + ": " + (autoSpawnEnabled ? "ON" : "OFF"), 5, 5, autoSpawnEnabled ? 0x00FF00 : 0xFF5555, true); //caresiz oe duzelt sunlari
				context.drawText(client.textRenderer, "Oto " +disconnecType+ ": " + (autoLeaveEnabled ? "ON" : "OFF"), 5, 18, autoLeaveEnabled ? 0x00FF00 : 0xFF5555, true); // caresiz oe duzelt sunlari
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

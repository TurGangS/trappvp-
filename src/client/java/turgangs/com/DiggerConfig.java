package turgangs.com;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DiggerConfig {
    public boolean autoLeaveEnabled = true;
    public boolean autoSpawnEnabled = true;
    public String commandOnLeave = "/spawn";
    public boolean showHUD = true;
    public boolean useDisconnectMethod = true;
    public boolean pickAllBeforeCommand = false;
    public int triggerItemCount = 1;
    public boolean useProximityInsteadOfChat = false;
    public boolean suppressGistMessages = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/diggerclient.json");

    public String[] triggerItems = new String[] {
            "item.minecraft.netherite_helmet",
            "item.minecraft.netherite_chestplate",
            "item.minecraft.netherite_leggings",
            "item.minecraft.netherite_boots",
            "item.minecraft.leather_helmet",
            "item.minecraft.leather_chestplate",
            "item.minecraft.leather_leggings",
            "item.minecraft.leather_boots",
            "item.minecraft.netherite_sword",
            "item.minecraft.diamond_helmet",
            "item.minecraft.diamond_sword",
            "item.minecraft.elytra"
    };

    public String[] proximityTriggerNames = new String[] {
            "TurGangS",
            "SetAvcisiTR",
            "SuikastciAhmet",
            "ItemciBaba",
            "TpciNecmi",
            "HEPSI ORNEKTIR",
            "BUNLARI YA SILIN YADA DEĞİŞTİRİN"
    };




    public static DiggerConfig load() {
        try {
            if (CONFIG_FILE.exists()) {
                return GSON.fromJson(new FileReader(CONFIG_FILE), DiggerConfig.class);
            } else {
                DiggerConfig config = new DiggerConfig();
                config.save();
                return config;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new DiggerConfig();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

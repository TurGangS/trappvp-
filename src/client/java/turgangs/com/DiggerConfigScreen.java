package turgangs.com;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.List;


public class DiggerConfigScreen {
    public static Screen create(Screen parent) {
        DiggerConfig config = DiggerClient.CONFIG;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("TurGangSMOD Settings"));

        ConfigCategory category = builder.getOrCreateCategory(Text.of("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Oto Ayrıl"), config.autoLeaveEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    config.autoLeaveEnabled = newValue;
                    DiggerClient.setAutoLeaveEnabled(newValue); // Add a setter in DiggerClient
                })
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Oto Spawn"), config.autoSpawnEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    config.autoSpawnEnabled = newValue;
                    DiggerClient.setAutoSpawnEnabled(newValue); // Add a setter in DiggerClient
                })
                .build());

        category.addEntry(entryBuilder.startStrField(Text.of("Tetiklenen komut"), config.commandOnLeave)
                .setDefaultValue("/spawn")
                .setSaveConsumer(newValue -> config.commandOnLeave = newValue)
                .build());
        category.addEntry(entryBuilder.startBooleanToggle(Text.of("HUD Göster"), config.showHUD)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.showHUD = newValue)
                .setTooltip(Text.of("Sol üstteki HUD'u açıp kapar. HUD kapalıysa bile mod çalışır."))
                .build());
        category.addEntry(entryBuilder.startStrList(
                        Text.of("Trigger İtemleri (Translation Keys)"),
                        List.of(config.triggerItems))
                .setSaveConsumer(list -> config.triggerItems = list.toArray(new String[0]))
                .setTooltip(Text.of("Örnek: item.minecraft.diamond_sword"))
                .build());



        builder.setSavingRunnable(() -> config.save());
        return builder.build();
    }
}

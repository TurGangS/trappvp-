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
                .setTitle(Text.of("TurGangS'ın Set Çalma Modu Ayarları"));

        ConfigCategory category = builder.getOrCreateCategory(Text.of("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        category.addEntry(entryBuilder.startBooleanToggle(Text.of("HUD Göster"), config.showHUD)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.showHUD = newValue)
                .setTooltip(Text.of("Sol üstteki HUD'u açıp kapar. HUD kapalıysa bile mod çalışır."))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Duyuruları Gizle"), config.suppressGistMessages)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.suppressGistMessages = newValue)
                .setTooltip(Text.of("Bu ayar aktifse, girişte gelen güncelleme mesajları gösterilmez. Her oyunu açtığınızda bu ayar sıfırlanır çünkü güncelleme duyuruluranı kaçırmanızı istemiyorum."))
                .build());


        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Oto Ayrıl"), config.autoLeaveEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    config.autoLeaveEnabled = newValue;
                    DiggerClient.setAutoLeaveEnabled(newValue);
                })
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Oto Komut"), config.autoSpawnEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    config.autoSpawnEnabled = newValue;
                    DiggerClient.setAutoSpawnEnabled(newValue);
                })
                .build());

        category.addEntry(entryBuilder.startStrField(Text.of("Tetiklenen komut"), config.commandOnLeave)
                .setDefaultValue("/spawn")
                .setSaveConsumer(newValue -> config.commandOnLeave = newValue)
                .setTooltip(Text.of("Herzaman komut olarak yazar. Evet, başına '/' koymasanız bile."))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Yakınlık Tabanlı Kaçış (Tüm Sunucular)"), config.useProximityInsteadOfChat)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.useProximityInsteadOfChat = newValue)
                .setTooltip(Text.of("Kapalıysa MaviBuğday için olan chat tabanlı tespit sistemi kullanır. Eğer Mavibuğday oynamıyorsanız açın."))
                .build());

        category.addEntry(entryBuilder.startStrList(
                        Text.of("Yakınlık Tetikleyici Oyuncu İsimleri"),
                        List.of(config.proximityTriggerNames))
                .setSaveConsumer(list -> config.proximityTriggerNames = list.toArray(new String[0]))
                .setTooltip(Text.of("Bu isimlerden biri 3 blok yakına gelirse kaçış tetiklenir."))
                .build());

        category.addEntry(entryBuilder.startStrList(
                        Text.of("Trigger İtemleri (Translation Keys)"),
                        List.of(config.triggerItems))
                .setSaveConsumer(list -> config.triggerItems = list.toArray(new String[0]))
                .setTooltip(Text.of("Örnek: item.minecraft.diamond_sword"))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Trigger'da Disconnect Kullan (açıklamayı oku)"), config.useDisconnectMethod)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.useDisconnectMethod = newValue)
                .setTooltip(Text.of("Aktifse oyuncu ışınlandığında disconnect atar, pasifse zırhı çıkarır."))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Text.of("Tüm Eşyalar Gelmeden Komut Gönderme"), config.pickAllBeforeCommand)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> config.pickAllBeforeCommand = newValue)
                .setTooltip(Text.of("Bütün tetikleyici eşyalar envantere girmeden oto komut çalışmaz."))
                .build());

        category.addEntry(entryBuilder.startIntField(Text.of("Trigger için Eşya Sayısı"), config.triggerItemCount)
                .setDefaultValue(1)
                .setMin(1)
                .setMax(64)
                .setSaveConsumer(newValue -> config.triggerItemCount = newValue)
                .setTooltip(Text.of("Tetikleme için gerekli eşya sayısı. *İSTEK ÜZERİNE EKLENECEK*"))
                .build());



        builder.setSavingRunnable(() -> config.save());
        return builder.build();
    }
}

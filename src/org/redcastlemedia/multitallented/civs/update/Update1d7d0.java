package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d7d0 {
    private Update1d7d0() {

    }

    public static String update() {
        updateConfig();
        updateTranslations();
        return "1.7.0";
    }

    private static void updateConfig() {
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            ArrayList<String> claimEffects = new ArrayList<>();
            claimEffects.add("block_build");
            claimEffects.add("block_break");
            claimEffects.add("block_fire");
            config.set("nation-claim-effects", claimEffects);
            config.set("power-per-nation-claim", 1);
            config.set("nation-formed-at-town-level", 3);
            config.save(configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    private static void updateTranslations() {
        File translationsFolder = new File(Civs.getInstance().getDataFolder(), "translations");
        if (!translationsFolder.exists()) {
            return;
        }
        File enFile = new File(translationsFolder, "en.yml");
        if (enFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(enFile);
                config.set("claims", "Claims");
                config.set("cant-build-in-nation", "You can't build inside $1 claimed land.");
                config.set("neutralized-claim", "$1's protections on this land have been removed.");
                config.set("alliance-chunk-claimed", "This land has been claimed for the alliance $1");
                config.set("nation-created", "$1 has reached a high enough level that it has become a nation!");
                config.save(enFile);
            } catch (Exception e) {

            }
        }
        // TODO finish translations for other locales
        File deFile = new File(translationsFolder, "de.yml");
        if (deFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(deFile);
                config.set("deposit-money", "Sie haben $1 auf $2s Bank eingezahlt");
                config.save(deFile);
            } catch (Exception e) {

            }
        }
        File esFile = new File(translationsFolder, "es.yml");
        if (esFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(esFile);
                config.set("deposit-money", "Has depositado $1 en el banco de $2");
                config.save(esFile);
            } catch (Exception e) {

            }
        }
        File ptBrFile = new File(translationsFolder, "pt_br.yml");
        if (ptBrFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(ptBrFile);
                config.set("deposit-money", "Você depositou $1 no banco de $2");
                config.save(ptBrFile);
            } catch (Exception e) {

            }
        }
        File huFile = new File(translationsFolder, "hu.yml");
        if (huFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(huFile);
                config.set("deposit-money", "A $1-at $2 bankjába helyezték");
                config.save(huFile);
            } catch (Exception e) {

            }
        }
        File zhFile = new File(translationsFolder, "zh.yml");
        if (zhFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(zhFile);
                config.set("deposit-money", "您将$1存入$2的银行");
                config.save(zhFile);
            } catch (Exception e) {

            }
        }
        File viFile = new File(translationsFolder, "vi.yml");
        if (viFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(viFile);
                config.set("deposit-money", "Bạn đã gửi $1 vào ngân hàng $2");
                config.save(viFile);
            } catch (Exception e) {

            }
        }
    }
}

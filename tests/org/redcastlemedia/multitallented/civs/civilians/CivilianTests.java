package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.TestUtil;

import static org.junit.Assert.assertEquals;

public class CivilianTests {


    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void localeTestShouldReturnProperLanguageString() {
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = new Civilian(TestUtil.player.getUniqueId(), "es");

        assertEquals("No se encontró ningún tipo de región",
                localeManager.getTranslation(civilian.getLocale(), "no-region-type-found"));
    }
}

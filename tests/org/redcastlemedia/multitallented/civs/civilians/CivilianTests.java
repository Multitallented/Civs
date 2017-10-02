package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
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
        //TODO actually pull locale settings for this test
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(TestUtil.player, "Join Message");

        CivilianManager civilianManager = CivilianManager.getInstance();

        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(playerJoinEvent);
        LocaleManager localeManager = LocaleManager.getInstance();

        assertEquals("No se encontró ningún tipo de región",
                localeManager.getTranslation(civilianManager.getCivilian(TestUtil.player.getUniqueId()).getLocale(), "no-region-type-found"));
    }
}

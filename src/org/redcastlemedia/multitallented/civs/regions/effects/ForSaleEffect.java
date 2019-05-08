package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterRegionEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.text.NumberFormat;
import java.util.Locale;

public class ForSaleEffect implements Listener {

    public static final String KEY = "buyable";

    @EventHandler
    public void onPlayerEnterRegionEvent(PlayerEnterRegionEvent event) {
        Region region = event.getRegion();
        RegionType regionType = event.getRegionType();
        if (region.getForSale() == -1 || !regionType.getEffects().containsKey(KEY)) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        sendTitleForSale(region, regionType, player);
    }

    public static void sendTitleForSale(Region region, RegionType regionType, Player player) {

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String title = Civs.NAME;
        String subTitle = LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-sale-set")
                .replace("$1", regionType.getName())
                .replace("$2", NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale())).format(region.getForSale()));
        player.sendTitle(title, subTitle, 5, 40, 5);
    }
}

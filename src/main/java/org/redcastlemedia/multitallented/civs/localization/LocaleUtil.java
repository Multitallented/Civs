package org.redcastlemedia.multitallented.civs.localization;

import java.util.List;

import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;

public final class LocaleUtil {
    private LocaleUtil() {

    }

    public static String getTranslationMaxRebuild(String maxItemOrGroupName, CivItem civItem,
                                                  String localCivItemName, Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        int limit;
        if (maxItemOrGroupName.equals(civItem.getProcessedName())) {
            limit = civItem.getCivMax();
            maxItemOrGroupName = civItem.getDisplayName(player);
        } else {
            limit = ConfigManager.getInstance().getGroups().get(maxItemOrGroupName);
            maxItemOrGroupName = LocaleManager.getInstance().getTranslation(player,
                    maxItemOrGroupName + LocaleConstants.GROUP_SUFFIX);
        }
        int currentAmount = civilian.getCountNonStashItems(civItem.getProcessedName()) +
                civilian.getCountStashItems(civItem.getProcessedName());
        return LocaleManager.getInstance().getTranslation(player, "max-rebuild-required")
                .replace("$1", "" + currentAmount)
                .replace("$2", "" + limit)
                .replace("$3", localCivItemName)
                .replace("$4", localCivItemName)
                .replace("$5", maxItemOrGroupName);
    }

    public static void getTranslationMaxItem(String maxLimit, CivItem civItem, Player player, List<String> lore) {
        int limit;
        if (maxLimit.equals(civItem.getProcessedName())) {
            limit = civItem.getCivMax();
            maxLimit = civItem.getDisplayName(player);
        } else {
            limit = ConfigManager.getInstance().getGroups().get(maxLimit);
            maxLimit = LocaleManager.getInstance().getTranslation(player,
                    maxLimit + LocaleConstants.GROUP_SUFFIX);
        }
        lore.add(LocaleManager.getInstance().getTranslation(player, "max-item")
                .replace("$1", maxLimit)
                .replace("$2", "" + limit));
    }
}

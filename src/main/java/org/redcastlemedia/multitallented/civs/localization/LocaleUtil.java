package org.redcastlemedia.multitallented.civs.localization;

import java.util.List;

import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;

public final class LocaleUtil {
    private LocaleUtil() {

    }

    public static void getTranslationMaxItem(String maxLimit, CivItem civItem, Player player, List<String> lore) {
        int limit;
        if (maxLimit.equals(civItem.getProcessedName())) {
            limit = civItem.getCivMax();
            maxLimit = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    civItem.getProcessedName() + LocaleConstants.NAME_SUFFIX);
        } else {
            limit = ConfigManager.getInstance().getGroups().get(maxLimit);
            maxLimit = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    maxLimit + LocaleConstants.GROUP_SUFFIX);
        }
        lore.add(LocaleManager.getInstance().getTranslationWithPlaceholders(player, "max-item")
                .replace("$1", maxLimit)
                .replace("$2", "" + limit));
    }
}

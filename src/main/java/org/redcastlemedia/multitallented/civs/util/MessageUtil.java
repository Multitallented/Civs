package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.skills.Skill;

public final class MessageUtil {
    private MessageUtil() {

    }

    public static void saveCivilianAndSendExpNotification(Player player, Civilian civilian, Skill skill, double exp) {
        if (exp > 0) {
            CivilianManager.getInstance().saveCivilian(civilian);

            if (!ConfigManager.getInstance().isSilentExp()) {
                String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        skill.getType() + LocaleConstants.SKILL_SUFFIX);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "exp-gained").replace("$1", "" + exp)
                        .replace("$2", localSkillName));
            }
        }
    }
}

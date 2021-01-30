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

            String localSkillName = LocaleManager.getInstance().getTranslation(player,
                    skill.getType() + LocaleConstants.SKILL_SUFFIX);
            String message = LocaleManager.getInstance().getTranslation(player,
                    "exp-gained").replace("$1", "" + exp)
                    .replace("$2", localSkillName);
            if (!ConfigManager.getInstance().isSilentExp()) {
                player.sendMessage(Civs.getPrefix() + message);
            } else {
                ActionBarUtil.sendActionBar(player, message);
            }
        }
    }
}

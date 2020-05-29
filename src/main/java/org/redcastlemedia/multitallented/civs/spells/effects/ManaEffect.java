package org.redcastlemedia.multitallented.civs.spells.effects;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

public class ManaEffect extends Effect {
    private int mana;
    private String target;
    private boolean silent;

    public ManaEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.mana = (int) Math.round(Spell.getLevelAdjustedValue(
                    section.getString(SpellEffectConstants.MANA, "5"), level, target, spell));
            this.silent = section.getBoolean(SpellConstants.SILENT, false);

            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = SpellConstants.SELF;
            }
        } else if (value instanceof String) {
            this.mana = (int) Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.target = SpellConstants.SELF;
            this.silent = false;
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return false;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        boolean hasMana = civilian.getMana() >= this.mana;
        if (!hasMana && !this.silent) {
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civilian.getCurrentClass().getType());
            String localManaName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    classType.getManaTitle());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "need-more-mana").replace("$1", "" + (this.mana - civilian.getMana()))
                    .replace("$2", localManaName));
        }
        return hasMana;
    }

    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        int maxMana = civilian.getCurrentClass().getMaxMana();
        civilian.setMana(Math.min(maxMana, Math.max(0, civilian.getMana() + this.mana)));
    }

    @Override
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        HashMap<String, Double> returnMap = new HashMap<>();
        if (!(target instanceof Player)) {
            return returnMap;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        returnMap.put("mana", (double) civilian.getMana());
        return returnMap;
    }

    public static String getManaBar(Civilian civilian) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return "";
        }
        CivClass civClass = civilian.getCurrentClass();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
        double currentMana = civilian.getMana();
        double maxMana = civClass.getMaxMana();
        final double LENGTH = 60;
        int progress = maxMana > 0 ? (int) Math.ceil(currentMana / maxMana * LENGTH) : (int) LENGTH;
        String localeMana = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                classType.getManaTitle());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.DARK_BLUE);
        stringBuilder.append(localeMana);
        stringBuilder.append(": (");
        stringBuilder.append(currentMana);
        stringBuilder.append("/");
        stringBuilder.append(maxMana);
        stringBuilder.append(") ");
        stringBuilder.append(ChatColor.BLUE);
        for (int i = 0; i < Math.min(progress, LENGTH); i++) {
            stringBuilder.append("|");
        }
        if (progress < LENGTH) {
            stringBuilder.append(ChatColor.RED);
            for (int i = progress; i < LENGTH; i++) {
                stringBuilder.append("|");
            }
        }
        return stringBuilder.toString();
    }

}

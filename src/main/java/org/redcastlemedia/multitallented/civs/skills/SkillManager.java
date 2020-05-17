package org.redcastlemedia.multitallented.civs.skills;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;

import lombok.Getter;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class SkillManager {
    private static SkillManager skillManager = null;

    @Getter
    private final HashMap<String, SkillType> skills = new HashMap<>();

    public static synchronized SkillManager getInstance() {
        if (skillManager == null) {
            skillManager = new SkillManager();
            if (Civs.getInstance() != null) {
                skillManager.loadAllSkills();
            }
        }
        return skillManager;
    }

    private void loadAllSkills() {
        final String SKILLS_FOLDER_NAME = "skills";
        File skillFolder = new File(Civs.dataLocation, SKILLS_FOLDER_NAME);
        boolean skillFolderExists = skillFolder.exists();
        String path = "resources." + ConfigManager.getInstance().getDefaultConfigSet() + "." + SKILLS_FOLDER_NAME;
        Reflections reflections = new Reflections(path , new ResourcesScanner());
        try {
            for (String fileName : reflections.getResources(Pattern.compile(".*\\.yml"))) {
                FileConfiguration config;
                if (skillFolderExists) {
                    config = FallbackConfigUtil.getConfigFullPath(
                            new File(skillFolder, fileName), "/" + fileName);
                } else {
                    config = FallbackConfigUtil.getConfigFullPath(null, "/" + fileName);
                }
                loadSkillType(config, fileName.substring(fileName.lastIndexOf("/") + 1).replace(".yml", ""));
            }
        } catch (ReflectionsException reflectionsException) {
            Civs.logger.log(Level.WARNING, "No Skill types found");
        }
        if (skillFolderExists) {
            for (File file : skillFolder.listFiles()) {
                String govName = file.getName().replace(".yml", "");
                if (skills.containsKey(govName)) {
                    continue;
                }
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);
                } catch (Exception e) {
                    Civs.logger.severe("Unable to load " + file.getName());
                    continue;
                }
                loadSkillType(config, govName);
            }
        }
    }

    private void loadSkillType(FileConfiguration config, String skillName) {
        if (!config.getBoolean("enabled")) {
            return;
        }
        SkillType skillType = new SkillType(skillName,
                config.getString("icon", "STONE"));
        skillType.setExpPerCategory(config.getDouble("exp-per-new-item", 100));
        skillType.setExpRepeatDecay(config.getDouble("exp-repeat-decay", 20));
        skillType.setMaxChance(config.getDouble("max-chance", 0.4));
        if (config.isSet("exceptions")) {
            Map<String, Double> exceptions = new HashMap<>();
            for (String key : config.getConfigurationSection("exceptions").getKeys(false)) {
                exceptions.put(key, config.getDouble("exceptions." + key, skillType.getExpPerCategory()));
            }
            skillType.setExceptions(exceptions);
        }
        skills.put(skillName, skillType);
    }

    public SkillType getSkillType(String name) {
        return skills.get(name);
    }
}

package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d6d0 {
    private Update1d6d0() {

    }
    public static String update() {
        updateTranslations();
        updateItemTypes();
        return "1.6.0";
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
                config.set("region-evolve", "$1 evolves into $2");
                config.set("income", "Income");
                config.set("income-desc", "Your $1 has earned $2 in the past day and $2 past week");
                config.set("item-limit", "You have $1 of $2 $3");
                config.set("warehouse-toggle-on", "Warehouse Supplies On");
                config.set("warehouse-toggle-off", "Warehouse Supplies Off");
                config.set("warehouse-toggle-desc", "Click to toggle warehouse supplies for this region");
                config.set("filter-online", "Filter for only online players");
                config.set("sort-alphabetical", "Sort alphabetical");
                config.set("sort-points", "Sort by kill points");
                config.set("sort-rank", "Sort by rank");
                config.set("karma-desc", "Karma is a measure of infamy. The lower the karma, the higher the reward for your death.");
                config.set("power-history", "Power history over the last $1 days:");
                config.set("location", "Location");
                config.set("plot7x7-name", "Plot7x7");
                config.set("plot7x7-desc", "A placeholder for regions 7x7 and smaller");
                config.set("plot11x11-name", "Plot11x11");
                config.set("plot11x11-desc", "A placeholder for regions 11x11 and smaller");
                config.set("plot15x15-name", "Plot15x15");
                config.set("plot15x15-desc", "A placeholder for regions 15x15 and smaller");
                config.set("plot19x19-name", "Plot19x19");
                config.set("plot19x19-desc", "A placeholder for regions 19x19 and smaller");
                config.set("jammer-built", "@{RED}[WARNING] A $1 has been built somewhere. Use caution when teleporting.");
                config.save(enFile);
            } catch (Exception e) {

            }
        }
    }

    private static void updateItemTypes() {
        File itemTypesFolder = new File(Civs.getInstance().getDataFolder(), "item-types");
        if (!itemTypesFolder.exists()) {
            return;
        }
        addPlots(itemTypesFolder);
    }

    private static void addPlots(File itemTypesFolder) {
        File adminInvisibleFolder = new File(itemTypesFolder, "admin-invisible");
        if (!adminInvisibleFolder.exists()) {
            return;
        }
        File plot7x7File = new File(adminInvisibleFolder, "plot7x7.yml");
        if (!plot7x7File.exists()) {
            try {
                plot7x7File.createNewFile();
                FileConfiguration config = new YamlConfiguration();
                config.set("type", "region");
                config.set("icon", "GLASS");
                config.set("shop-icon", "GLASS");
                config.set("name", "Plot7x7");
                config.set("qty", 1);
                config.set("is-in-shop", false);
                config.set("level", 1);
                ArrayList<String> preReqs = new ArrayList<>();
                preReqs.add("member=settlement:hamlet:village:town:city:metropolis");
                config.set("pre-reqs", preReqs);
                ArrayList<String> towns = new ArrayList<>();
                preReqs.add("settlement");
                preReqs.add("hamlet");
                preReqs.add("village");
                preReqs.add("town");
                preReqs.add("city");
                preReqs.add("metropolis");
                config.set("towns", towns);
                config.set("build-radius", 3);
                ArrayList<String> effects = new ArrayList<>();
                preReqs.add("block_break");
                preReqs.add("block_build");
                preReqs.add("plot");
                config.set("effects", effects);
                config.save(plot7x7File);
            } catch (Exception e) {

            }
        }
        File plot11x11File = new File(adminInvisibleFolder, "plot7x7.yml");
        if (!plot11x11File.exists()) {
            try {
                plot11x11File.createNewFile();
                FileConfiguration config = new YamlConfiguration();
                config.set("type", "region");
                config.set("icon", "GLASS");
                config.set("shop-icon", "GLASS");
                config.set("name", "Plot11x11");
                config.set("qty", 1);
                config.set("is-in-shop", false);
                config.set("level", 1);
                ArrayList<String> preReqs = new ArrayList<>();
                preReqs.add("member=settlement:hamlet:village:town:city:metropolis");
                config.set("pre-reqs", preReqs);
                ArrayList<String> towns = new ArrayList<>();
                preReqs.add("settlement");
                preReqs.add("hamlet");
                preReqs.add("village");
                preReqs.add("town");
                preReqs.add("city");
                preReqs.add("metropolis");
                config.set("towns", towns);
                config.set("build-radius", 5);
                ArrayList<String> effects = new ArrayList<>();
                preReqs.add("block_break");
                preReqs.add("block_build");
                preReqs.add("plot");
                config.set("effects", effects);
                config.save(plot11x11File);
            } catch (Exception e) {

            }
        }
        File plot15x15File = new File(adminInvisibleFolder, "plot7x7.yml");
        if (!plot15x15File.exists()) {
            try {
                plot15x15File.createNewFile();
                FileConfiguration config = new YamlConfiguration();
                config.set("type", "region");
                config.set("icon", "GLASS");
                config.set("shop-icon", "GLASS");
                config.set("name", "Plot15x15");
                config.set("qty", 1);
                config.set("is-in-shop", false);
                config.set("level", 1);
                ArrayList<String> preReqs = new ArrayList<>();
                preReqs.add("member=hamlet:village:town:city:metropolis");
                config.set("pre-reqs", preReqs);
                ArrayList<String> towns = new ArrayList<>();
                preReqs.add("hamlet");
                preReqs.add("village");
                preReqs.add("town");
                preReqs.add("city");
                preReqs.add("metropolis");
                config.set("towns", towns);
                config.set("build-radius", 7);
                ArrayList<String> effects = new ArrayList<>();
                preReqs.add("block_break");
                preReqs.add("block_build");
                preReqs.add("plot");
                config.set("effects", effects);
                config.save(plot15x15File);
            } catch (Exception e) {

            }
        }
        File plot19x19File = new File(adminInvisibleFolder, "plot7x7.yml");
        if (!plot19x19File.exists()) {
            try {
                plot19x19File.createNewFile();
                FileConfiguration config = new YamlConfiguration();
                config.set("type", "region");
                config.set("icon", "GLASS");
                config.set("shop-icon", "GLASS");
                config.set("name", "Plot19x19");
                config.set("qty", 1);
                config.set("is-in-shop", false);
                config.set("level", 1);
                ArrayList<String> preReqs = new ArrayList<>();
                preReqs.add("member=village:town:city:metropolis");
                config.set("pre-reqs", preReqs);
                ArrayList<String> towns = new ArrayList<>();
                preReqs.add("village");
                preReqs.add("town");
                preReqs.add("city");
                preReqs.add("metropolis");
                config.set("towns", towns);
                config.set("build-radius", 9);
                ArrayList<String> effects = new ArrayList<>();
                preReqs.add("block_break");
                preReqs.add("block_build");
                preReqs.add("plot");
                config.set("effects", effects);
                config.save(plot19x19File);
            } catch (Exception e) {

            }
        }
    }
}

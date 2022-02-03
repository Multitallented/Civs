package org.redcastlemedia.multitallented.civs.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import ru.endlesscode.mimic.classes.BukkitClassSystem;

public class MimicClassProvider extends BukkitClassSystem {
    private static String CLASS_PROVIDER_ID = "CIVS_CLASS_PROVIDER";

    public static class Provider extends BukkitClassSystem.Provider {

        public Provider() {
            super(CLASS_PROVIDER_ID); // Specify your implementation ID here
        }

        @NotNull
        @Override
        public BukkitClassSystem getSystem(@NotNull Player player) {
            return new MimicClassProvider(player);
        }
    }

    public MimicClassProvider(@NotNull Player player) {
        super(player);
    }

    @NotNull
    @Override
    public List<String> getClasses() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(super.getPlayer().getUniqueId());
        Set<String> classes = new HashSet<>();
        for (CivClass civClass : civilian.getCivClasses()) {
            classes.add(civClass.getType());
        }
        return new ArrayList<>(classes);
    }

    @Nullable
    @Override
    public String getPrimaryClass() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(super.getPlayer().getUniqueId());
        if (civilian.getCurrentClass() == null) {
            return super.getPrimaryClass();
        }
        return civilian.getCurrentClass().getType();
    }
}

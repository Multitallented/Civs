package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.items.CivItem;

import java.util.List;

public class SpellType extends CivItem {

    public SpellType(List<String> reqs,
                     String name,
                     Material material,
                     int damage,
                     int qty,
                     int min,
                     int max,
                     double price,
                     String permission,
                     List<String> description,
                     List<String> groups) {
        super(reqs,
                false,
                ItemType.SPELL,
                name,
                material,
                damage,
                qty,
                min,
                max,
                price,
                permission,
                description,
                groups);
    }
}

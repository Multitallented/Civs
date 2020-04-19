package org.redcastlemedia.multitallented.civs.nations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;


public class Nation {
    @Getter @Setter
    private String name;
    @Getter @Setter
    private ItemStack icon;
    @Getter
    private Set<String> members = new HashSet<>();
    @Getter @Setter
    private String capitol;
    @Getter @Setter
    private UUID lastRenamedBy;
    @Getter
    private HashSet<String> effects = new HashSet<>();
    @Getter
    private HashMap<UUID, HashMap<String, ChunkClaim>> nationClaims = new HashMap<>();
    @Getter
    private String desc;
    private String loreTitle;
    private List<String> lorePages;

    public void setLore(ItemStack itemStack) {
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
        loreTitle = bookMeta.getTitle();
        lorePages = bookMeta.getPages();
        if (!lorePages.isEmpty()) {
            desc = lorePages.get(0).substring(0, Math.min(200, lorePages.get(0).length()));
        }
    }

    public ItemStack getLore() {
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
        bookMeta.setDisplayName(name);
        bookMeta.setPages(lorePages);
        bookMeta.setTitle(loreTitle);
        itemStack.setItemMeta(bookMeta);
        return itemStack;
    }

    public CVItem getIconAsCVItem(Civilian civilian) {
        CVItem cvItem = CVItem.createFromItemStack(icon);
        cvItem.setDisplayName(name);
        cvItem.setLore(Util.textWrap(civilian, desc));
        return cvItem;
    }

    public int getPower() {
        int power = 0;
        for (String townName : members) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town != null) {
                power += town.getPower();
            }
        }
        return power;
    }
}

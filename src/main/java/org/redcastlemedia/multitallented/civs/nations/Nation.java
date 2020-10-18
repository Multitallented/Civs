package org.redcastlemedia.multitallented.civs.nations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;


public class Nation {
    @Getter @Setter
    private String name;
    @Setter
    private ItemStack icon;
    @Getter @Setter
    private Set<String> members = new HashSet<>();
    @Getter @Setter
    private String capitol;
    @Getter @Setter
    private UUID lastRenamedBy;
    @Getter @Setter
    private HashSet<String> effects = new HashSet<>();
    @Getter @Setter
    private HashMap<UUID, HashMap<String, ChunkClaim>> nationClaims = new HashMap<>();
    @Getter
    private String desc;
    private String loreTitle;
    private List<String> lorePages;
    @Getter
    private Set<Town> nationApplications = new HashSet<>();

    public void setLore(ItemStack itemStack) {
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
        loreTitle = bookMeta.getTitle();
        lorePages = bookMeta.getPages();
        if (!lorePages.isEmpty()) {
            desc = lorePages.get(0).substring(0, Math.min(200, lorePages.get(0).length()));
        }
    }

    public ItemStack getLore() {
        if (lorePages == null || lorePages.isEmpty()) {
            return null;
        }
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
        bookMeta.setDisplayName(name);
        bookMeta.setPages(lorePages);
        bookMeta.setTitle(loreTitle);
        itemStack.setItemMeta(bookMeta);
        return itemStack;
    }

    public ItemStack getIcon() {
        return getIconAsCVItem().createItemStack();
    }

    public ItemStack getRawIcon() {
        return icon;
    }

    public CVItem getIconAsCVItem() {
        if (icon != null) {
            CVItem currentIcon = CVItem.createFromItemStack(icon);
            currentIcon.setDisplayName(name);
            if (desc != null) {
                currentIcon.setLore(Util.textWrap(desc));
            }
            if (currentIcon.getMat() == null) {
                currentIcon.setMat(Material.STONE);
            }
            return currentIcon;
        }
        if (members.isEmpty()) {
            CVItem defaultItem = CVItem.createCVItemFromString(Material.STONE.name());
            defaultItem.setDisplayName(name);
            defaultItem.setLore(Util.textWrap(desc));
            return defaultItem;
        }
        String townName = members.iterator().next();
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            CVItem defaultItem = CVItem.createCVItemFromString(Material.STONE.name());
            defaultItem.setDisplayName(name);
            defaultItem.setLore(Util.textWrap(desc));
            return defaultItem;
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        CVItem returnItem = townType.getShopIcon(ConfigManager.getInstance().getDefaultLanguage());
        returnItem.setDisplayName(name);
        returnItem.setLore(Util.textWrap(desc));
        return returnItem;
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

    public int getMaxPower() {
        int maxPower = 0;
        for (String townName : members) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town != null) {
                maxPower += town.getMaxPower();
            }
        }
        return maxPower;
    }

    public int getClaimCount() {
        int total = 0;
        for (Map<String, ChunkClaim> worldClaims : nationClaims.values()) {
            total += worldClaims.size();
        }
        return total;
    }
}

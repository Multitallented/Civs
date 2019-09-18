package org.redcastlemedia.multitallented.civs.menus.alliance;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.Menu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class AllianceMenu implements CustomMenu {
    private int size;
    private MenuIcon menuIcon;
    private MenuIcon renameIcon;
    private MenuIcon lastRenameIcon;
    private MenuIcon leaveAllianceIcon;
    private int memberStartIndex;
    private int backIndex;

    @Override
    public Inventory createMenu(Civilian civilian) {
        Alliance alliance = (Alliance) MenuManager.getInstance().getData(civilian.getUuid(), "alliance");
        int instanceSize = size;
        if (size == -1) {
            instanceSize = MenuManager.getInventorySize(alliance.getMembers().size()) + 9;
        }
        Inventory inventory = Bukkit.createInventory(null, instanceSize, getKey());
        inventory.setItem(menuIcon.getIndex(), menuIcon.createCVItem(civilian.getLocale()).createItemStack());
        inventory.setItem(renameIcon.getIndex(), renameIcon.createCVItem(civilian.getLocale()).createItemStack());

        if (alliance.getLastRenamedBy() != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(alliance.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = lastRenameIcon.createCVItem(civilian.getLocale());
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "last-renamed-by").replace("$1", offlinePlayer.getName()));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                inventory.setItem(lastRenameIcon.getIndex(), is);
            }
        }

        boolean isOwnerOfTown = false;
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town.getPeople().containsKey(civilian.getUuid()) &&
                    town.getPeople().get(civilian.getUuid()).contains("owner")) {
                isOwnerOfTown = true;
                break;
            }
        }

        if (isOwnerOfTown) {
            inventory.setItem(leaveAllianceIcon.getIndex(), leaveAllianceIcon.createCVItem(civilian.getLocale()).createItemStack());
        }

        inventory.setItem(backIndex, MenuManager.getInstance().getBackButton(civilian));

        // TODO create paginated list of members

        return inventory;
    }
    @Override
    public void loadConfig(FileConfiguration config, int size) {
        this.size = size;
        this.menuIcon = new MenuIcon(config.getConfigurationSection("icon"));
        this.renameIcon = new MenuIcon(config.getConfigurationSection("rename"));
        this.lastRenameIcon = new MenuIcon(config.getConfigurationSection("last-rename"));
        this.leaveAllianceIcon = new MenuIcon(config.getConfigurationSection("leave-alliance"));
        this.backIndex = config.getInt("back.index", 8);
        this.memberStartIndex = config.getInt("members.index", 9);
    }

    @Override
    public String getKey() {
        return "Alliance";
    }

    @Override
    public String getFileName() {
        return "alliance";
    }
}

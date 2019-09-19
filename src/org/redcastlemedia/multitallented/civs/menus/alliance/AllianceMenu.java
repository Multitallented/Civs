package org.redcastlemedia.multitallented.civs.menus.alliance;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class AllianceMenu extends CustomMenu {

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Alliance alliance = (Alliance) MenuManager.getData(civilian.getUuid(), "alliance");
        int page = (int) MenuManager.getData(civilian.getUuid(), "page");
        if (menuIcon.getKey().equals("members")) {
            int startIndex = page * menuIcon.getIndex().size();
            String[] memberNames = new String[alliance.getMembers().size()];
            memberNames = alliance.getMembers().toArray(memberNames);
            String townName = memberNames[startIndex + count];
            Town town = TownManager.getInstance().getTown(townName);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            return cvItem.createItemStack();
        }
        if (menuIcon.getKey().equals("prev")) {
            if (page < 2) {
                return new ItemStack(Material.AIR);
            }
            return super.createItemStack(civilian, menuIcon, count);
        }
        if (menuIcon.getKey().equals("next")) {
            if ((page + 1) * menuIcon.getIndex().size() > alliance.getMembers().size()) {
                return new ItemStack(Material.AIR);
            }
            return super.createItemStack(civilian, menuIcon, count);
        }
        if (menuIcon.getKey().equals("last-rename")) {
            if (alliance == null || alliance.getLastRenamedBy() == null) {
                return new ItemStack(Material.AIR);
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(alliance.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = menuIcon.createCVItem(civilian.getLocale());
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "last-renamed-by").replace("$1", offlinePlayer.getName()));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                return is;
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
        if (menuIcon.getKey().equals("rename") ||
                menuIcon.getKey().equals("leave-alliance")) {
            if (!isOwnerOfTown) {
                return new ItemStack(Material.AIR);
            }
            return super.createItemStack(civilian, menuIcon, count);
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getKey() {
        return "Alliance";
    }

    @Override
    public String getFileName() {
        return "alliance";
    }

    @Override
    public void doAction(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        // TODO design this
    }
}

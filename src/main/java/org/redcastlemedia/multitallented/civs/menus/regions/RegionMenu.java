package org.redcastlemedia.multitallented.civs.menus.regions;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.regions.effects.ForSaleEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "region") @SuppressWarnings("unused")
public class RegionMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (!params.containsKey("region")) {
            return data;
        }
        Region region = RegionManager.getInstance().getRegionById(params.get("region"));
        data.put("region", region);
        StringBuilder failingUpkeeps = new StringBuilder();
        for (Integer i : region.getFailingUpkeeps()) {
            failingUpkeeps.append(i).append(",");
        }
        if (!region.getFailingUpkeeps().isEmpty()) {
            failingUpkeeps.substring(0, failingUpkeeps.length() - 1);
            data.put("failingUpkeeps", failingUpkeeps.toString());
        } else {
            data.put("failingUpkeeps", "");
        }
        data.put("regionTypeName", region.getType());
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player != null && !StructureUtil.hasBoundingBoxShown(civilian.getUuid())) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean infiniteBoundingBox = params.containsKey(Constants.INFINITE_BOUNDING_BOX);
            if (player.getLocation().getWorld().equals(region.getLocation().getWorld()) &&
                    player.getLocation().distanceSquared(region.getLocation()) < 400) {
                StructureUtil.showGuideBoundingBox(player, region.getLocation(), regionType, infiniteBoundingBox);
            }
        }
        if (region.shouldTick()) {
            data.put("cooldown", Util.formatTime(player, 0));
        } else {
            data.put("cooldown", Util.formatTime(player, region.getSecondsTillNextTick()));
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(
                (String) MenuManager.getData(civilian.getUuid(), "regionTypeName"));
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || region == null) {
            return new ItemStack(Material.AIR);
        }
        Map<UUID, String> regionPeople = region.getPeople();
        boolean isMember = region.getRawPeople().containsKey(civilian.getUuid());
        boolean isOwner = isMember &&
                region.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER);
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        boolean viewMembers = Util.hasOverride(region, civilian, town) ||
                (regionPeople.get(civilian.getUuid()) != null &&
                regionPeople.get(civilian.getUuid()).contains(Constants.OWNER));
        int personCount = 0;
        for (String role : region.getRawPeople().values()) {
            if (role.contains(Constants.OWNER) || role.contains(Constants.MEMBER)) {
                personCount++;
            }
        }
        boolean hasUpkeepsOrInput = false;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if (!regionUpkeep.getInputs().isEmpty() || !regionUpkeep.getReagents().isEmpty()) {
                hasUpkeepsOrInput = true;
                break;
            }
        }
        boolean canSeeSellOptions = personCount == 1 && regionType.getEffects().containsKey(ForSaleEffect.KEY);
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = regionType.getShopIcon(player);
            cvItem.setDisplayName(regionType.getDisplayName(player));
            cvItem.setLore(regionType.getLore(player, false));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("upkeep-not-working".equals(menuIcon.getKey()) &&
                region.getFailingUpkeeps().size() < regionType.getUpkeeps().size() &&
                region.getMissingBlocks().isEmpty()) {
            return new ItemStack(Material.AIR);
        } else if ("upkeep-working".equals(menuIcon.getKey()) &&
                (region.getFailingUpkeeps().size() >= regionType.getUpkeeps().size() ||
                !region.getMissingBlocks().isEmpty())) {
            return new ItemStack(Material.AIR);
        } else if ("destroy".equals(menuIcon.getKey())) {
            boolean isIndestrucible = region.getEffects().containsKey("indestructible");
            boolean isAdmin = Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION);
            if (!isAdmin && (isIndestrucible || !isOwner)) {
                return new ItemStack(Material.AIR);
            }
        } else if ("people".equals(menuIcon.getKey())) {
            if (!viewMembers && !isMember) {
                return new ItemStack(Material.AIR);
            }
        } else if ("add-person".equals(menuIcon.getKey())) {
            if (!viewMembers) {
                return new ItemStack(Material.AIR);
            }
        } else if ("sale".equals(menuIcon.getKey())) {
            if (!canSeeSellOptions || !isOwner) {
                return new ItemStack(Material.AIR);
            }
        } else if ("cancel-sale".equals(menuIcon.getKey())) {
            if (!canSeeSellOptions || region.getForSale() == -1 || !isOwner) {
                return new ItemStack(Material.AIR);
            }
        } else if ("buy-region".equals(menuIcon.getKey())) {
            if (isOwner || region.getRawPeople().containsKey(civilian.getUuid()) ||
                    region.getForSale() == -1 ||
                    civilian.isAtMax(regionType) != null) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", regionType.getDisplayName(player))
                    .replace("$2", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc())));

            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("location".equals(menuIcon.getKey())) {
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(region.getLocation().getWorld().getName() + " " +
                    (int) region.getLocation().getX() + "x, " +
                    (int) region.getLocation().getY() + "y, " +
                    (int) region.getLocation().getZ() + "z");
            if (town != null) {
                cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                        menuIcon.getDesc()).replace("$1", town.getName())));
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("region-type".equals(menuIcon.getKey())) {
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(regionType.getDisplayName(player));
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc())));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("income".equals(menuIcon.getKey())) {
            boolean isAdmin = Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION);
            if (!isAdmin && !isOwner) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()));
            HashMap<Integer, Integer> upkeepsWithinLastDay = region.getNumberOfUpkeepsWithin24Hours();
            HashMap<Integer, Integer> upkeepsWithinLastWeek = region.getNumberOfUpkeepsWithin1Week();
            double lastDayIncome = 0;
            double lastWeekIncome = 0;
            int i = 0;
            for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
                if (regionUpkeep.getPayout() == 0) {
                    i++;
                    continue;
                }
                if (upkeepsWithinLastDay.containsKey(i)) {
                    lastDayIncome += (double) upkeepsWithinLastDay.get(i) * regionUpkeep.getPayout();
                }
                if (upkeepsWithinLastWeek.containsKey(i)) {
                    lastWeekIncome += (double) upkeepsWithinLastWeek.get(i) * regionUpkeep.getPayout();
                }
                i++;
            }
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", regionType.getDisplayName(player))
                    .replace("$2", NumberFormat.getCurrencyInstance().format(lastDayIncome))
                    .replace("$3", NumberFormat.getCurrencyInstance().format(lastWeekIncome))));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("warehouse-enabled".equals(menuIcon.getKey())) {
            if (!region.isWarehouseEnabled() || !hasUpkeepsOrInput || !isOwner) {
                return new ItemStack(Material.AIR);
            }
        } else if ("warehouse-disabled".equals(menuIcon.getKey())) {
            if (region.isWarehouseEnabled() || !hasUpkeepsOrInput || !isOwner) {
                return new ItemStack(Material.AIR);
            }
        } else if ("missing-blocks".equals(menuIcon.getKey())) {
            if (region.getMissingBlocks().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player, menuIcon.getName())
                    .replace("$1", regionType.getDisplayName(player)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (actionString.equals("cancel-sale")) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            player.performCommand("cv sell");
            return true;
        } else if (actionString.equals("toggle-warehouse")) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
            region.setWarehouseEnabled(!region.isWarehouseEnabled());
            RegionManager.getInstance().saveRegion(region);
            return true;
        } else if (actionString.equals("buy-region")) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
            sellRegion(civilian, region);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    private void sellRegion(Civilian civilian, Region region) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (Civs.econ == null || !Civs.econ.has(player, region.getForSale())) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "not-enough-money").replace("$1", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));
            return;
        }
        region.getRawPeople().clear();
        region.getRawPeople().put(civilian.getUuid(), Constants.OWNER);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        Civs.econ.withdrawPlayer(player, region.getForSale());
        String localName = regionType.getDisplayName(player);

        Set<UUID> owners = region.getOwners();
        int split = owners.size();
        if (split > 0 && Civs.econ != null) {
            double cutOfTheSale = region.getForSale() / (double) split;
            for (UUID ownerUuid : owners) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUuid);
                if (offlinePlayer.hasPlayedBefore()) {
                    Civs.econ.depositPlayer(offlinePlayer, cutOfTheSale);

                    if (offlinePlayer.isOnline()) {
                        Civilian ownerCiv = CivilianManager.getInstance().getCivilian(offlinePlayer.getUniqueId());
                        ((Player) offlinePlayer).sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                                offlinePlayer, "region-sold").replace("$1", localName)
                                .replace("$2", player.getDisplayName()).replace("$3", Util.getNumberFormat(cutOfTheSale, ownerCiv.getLocale())));
                    }
                }
            }
        }

        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "region-bought").replace("$1", localName)
                .replace("$2", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));
        region.setForSale(-1);
        RegionManager.getInstance().saveRegion(region);
    }
}

package org.redcastlemedia.multitallented.civs.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class WorldEditSessionListener {

    public static void init() {
        WorldEdit.getInstance().getEventBus().register(new Object() {

            @Subscribe
            public void onEditSessionEvent(EditSessionEvent event) {
                if (event.getStage() != EditSession.Stage.BEFORE_CHANGE) {
                    return;
                }
                if (event.getWorld() == null || Util.isDisallowedByWorld(event.getWorld().getName())) {
                    return;
                }

                event.setExtent(new CivsExtent(event.getExtent(), event.getWorld()));
            }
        });

    }

    private static class CivsExtent extends AbstractDelegateExtent {

        private org.bukkit.World world;

        protected CivsExtent(Extent extent, World world) {
            super(extent);
            this.world = Bukkit.getWorld(world.getName());
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
            if (!skip(location)) {
                return super.setBlock(location, block);
            }
            return false;
        }

        public boolean skip(BlockVector3 b) {
            RegionManager regionManager = RegionManager.getInstance();
            Region regionAt = regionManager.getRegionAt(new Location(world, b.getX(), b.getY(), b.getZ()));
            return regionAt != null;
        }
    }

}

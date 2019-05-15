package org.redcastlemedia.multitallented.civs.util;

import org.dynmap.DynmapCommonAPI;

public final class DynmapUtil {
    public static DynmapCommonAPI dynmapCommonAPI = null;

    private DynmapUtil() {

    }

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return true;
        }
        return dynmapCommonAPI.markerAPIInitialized();
    }
}

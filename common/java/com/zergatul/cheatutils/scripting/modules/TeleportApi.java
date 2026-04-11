package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.TeleportHack;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class TeleportApi {

    @MethodDescription("""
            Attempts to teleport you to crosshair. If you target a sky, you will be teleported in that direction.
            Your feet will be placed at crosshair location.
            If teleport to specified distance is not possible, it backs off toward you until it finds a valid position.
            Automatically calculates amount of packets it has to send. Uses vanilla-style packet budget estimate.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean toCrosshair(double distance) {
        if (isInvalidDistance(distance)) {
            return false;
        }

        return TeleportHack.instance.teleportToCrosshair(distance);
    }

    @MethodDescription("""
            Attempts to teleport you to crosshair. If you target a sky, you will be teleported in that direction.
            Your feet will be placed at crosshair location.
            If teleport to specified distance is not possible, it backs off toward you until it finds a valid position.
            Sends specified number of MovePlayer packets, including the final position packet.
            It always sends at least one packet, no matter what parameter value is.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean toCrosshair(double distance, int packets) {
        if (isInvalidDistance(distance)) {
            return false;
        }

        return TeleportHack.instance.teleportToCrosshair(distance, packets);
    }

    @MethodDescription("""
            Attempts to teleport you vertically by specified distance.
            Positive distance teleports up, negative distance teleports down.
            If teleport to specified distance is not possible, it backs off toward you until it finds a valid position.
            Automatically calculates amount of packets it has to send. Uses vanilla-style packet budget estimate.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double distance) {
        if (isInvalidDistance(distance)) {
            return false;
        }

        return TeleportHack.instance.verticalTeleport(distance);
    }

    @MethodDescription("""
            Attempts to teleport you vertically by specified distance.
            Positive distance teleports up, negative distance teleports down.
            If teleport to specified distance is not possible, it backs off toward you until it finds a valid position.
            Sends specified number of MovePlayer packets, including the final position packet.
            It always sends at least one packet, no matter what parameter value is.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double distance, int packets) {
        if (isInvalidDistance(distance)) {
            return false;
        }

        return TeleportHack.instance.verticalTeleport(distance, packets);
    }

    @MethodDescription("""
            Attempts to find a valid vertical teleport position in specified range.
            Both distances should have the same sign: positive values search up, negative values search down.
            If findSurface is true, the target must have a block below your feet.
            Automatically calculates amount of packets it has to send. Uses vanilla-style packet budget estimate.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double fromDistance, double toDistance, boolean findSurface) {
        if (isInvalidDistance(fromDistance) || isInvalidDistance(toDistance)) {
            return false;
        }

        return TeleportHack.instance.verticalTeleport(fromDistance, toDistance, findSurface);
    }

    @MethodDescription("""
            Attempts to find a valid vertical teleport position in specified range.
            Both distances should have the same sign: positive values search up, negative values search down.
            If findSurface is true, the target must have a block below your feet.
            Sends specified number of MovePlayer packets, including the final position packet.
            It always sends at least one packet, no matter what parameter value is.
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean vertical(double fromDistance, double toDistance, boolean findSurface, int packets) {
        if (isInvalidDistance(fromDistance) || isInvalidDistance(toDistance)) {
            return false;
        }

        return TeleportHack.instance.verticalTeleport(fromDistance, toDistance, findSurface, packets);
    }

    private boolean isInvalidDistance(double distance) {
        return Double.isNaN(distance) || Double.isInfinite(distance);
    }
}
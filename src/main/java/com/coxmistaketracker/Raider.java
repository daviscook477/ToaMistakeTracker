package com.coxmistaketracker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/**
 * Encapsulating class for a {@link Player} and other relevant metadata in a raid.
 */
public class Raider {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NonNull
    private Player player;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private WorldPoint previousWorldLocation;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private WorldPoint previousWorldLocationForOverlay;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean isDead;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int deadTick;

    Raider(@NonNull Player player) {
        this.player = player;
    }

    public String getName() {
        return player.getName();
    }

    public WorldPoint getCurrentWorldLocation() {
        return player.getWorldLocation();
    }
}

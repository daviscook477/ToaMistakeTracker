package com.coxmistaketracker.detector.tracker;

import com.coxmistaketracker.Raider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ProjectileMoved;

import java.util.HashSet;
import java.util.Set;

import static com.coxmistaketracker.CoxMistakeTrackerPlugin.CYCLES_PER_GAME_TICK;

@RequiredArgsConstructor
@Slf4j
public class GroundProjectileTracker extends DelayedObjectsTracker<Projectile> {

    private final Set<Projectile> trackedProjectiles = new HashSet<>();

    private int projectileId;
    private int radius;

    public GroundProjectileTracker(int projectileId, int radius) {
        this.projectileId = projectileId;
        this.radius = radius;
    }

    /**
     * Tracks a Projectile from the specified ProjectileMoved event
     *
     * @param event    The event containing the Projectile and metadata
     * @param activationTick The activationTick for when this projectile activates its overhead detection
     */
    public void trackProjectile(@NonNull ProjectileMoved event, @NonNull Integer activationTick) {
        Projectile projectile = event.getProjectile();
        if (projectile.getId() != projectileId) {
            log.debug("Id not matched");
            return;
        }

        if (!hasEnoughRemainingCycles(projectile)) {
            // There's no way there's a new projectile that only has at most 1 game tick left. It's probably
            // hanging around from a previous attack, so let's ignore
            log.debug("Not enough cycles");
            return;
        }

        log.debug("Tracking projectile " + projectile.getId() + " with " + activationTick + " activation tick");
        put(activationTick, projectile);
        trackedProjectiles.add(projectile);
    }

    @Override
    public void onGameTick(@NonNull Integer gameTick) {
        super.onGameTick(gameTick);

        // Clear state of projectiles that have at most 1 game tick left, as they can't be added anyway
        trackedProjectiles.removeIf(projectile ->!hasEnoughRemainingCycles(projectile));
    }

    @Override
    public void clear() {
        super.clear();
        trackedProjectiles.clear();
    }

    public Set<Projectile> getActiveProjectiles() {
        return getActiveObjects();
    }

    public boolean didGetHit(Raider raider, Client client) {
        if (getActiveProjectiles().isEmpty()) {
            return false;
        }

        for (Projectile projectile : getActiveProjectiles()) {
            WorldPoint projectilePoint = WorldPoint.fromLocal(client, projectile.getTarget());
            WorldPoint raiderPoint = raider.getPlayer().getWorldLocation();
            log.info("projectile @ " + projectilePoint.toString() + " player @ " + raiderPoint.toString());
            int xDist = projectilePoint.getX() - raiderPoint.getX();
            int yDist = projectilePoint.getY() - raiderPoint.getY();
            if (Math.abs(xDist) <= radius && Math.abs(yDist) <= radius) {
                log.info("projectile hit");
                return true;
            }
        }

        return false;
    }

    private boolean hasEnoughRemainingCycles(Projectile projectile) {
        // There's no way there's a new projectile that only has at most 1 game tick left. It's probably
        // hanging around from a previous attack, so let's ignore
        return projectile.getRemainingCycles() > CYCLES_PER_GAME_TICK;
    }
}

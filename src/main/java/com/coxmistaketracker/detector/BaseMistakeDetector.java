package com.coxmistaketracker.detector;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.RaidState;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.tracker.VengeanceTracker;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface for detecting mistakes during The Tombs of Amascut
 */
@Slf4j
public abstract class BaseMistakeDetector {

    protected static final int CYCLES_PER_GAME_TICK = Constants.GAME_TICK_LENGTH / Constants.CLIENT_TICK_LENGTH;

    @Inject
    @Setter
    protected Client client;

    @Inject
    @Setter
    protected EventBus eventBus;

    @Inject
    @Setter
    protected RaidState raidState;

    @Inject
    @Setter
    protected VengeanceTracker vengeanceTracker;

    /**
     * Used to tell a detector to start listening for events.
     */
    public void startup() {
        log.debug("Starting detector " + this.getClass().getName());
        cleanup();
        eventBus.register(this);
    }

    /**
     * Shutdown and cleanup state. This is always called when the plugin is shutdown, or when a detector is finished.
     */
    public void shutdown() {
        log.debug("Stopping detector " + this.getClass().getName());
        eventBus.unregister(this);
        cleanup();
    }

    /**
     * Cleanup all relevant state in the detector. This is called during startup to reset state, and shutdown to cleanup
     * This is also called for active detectors whenever raiders are loaded, which is during room transitions and room
     * resets (wipes).
     */
    public abstract void cleanup();

    /**
     * Retrieve the raid room that this detector should startup in. A null value means *all* rooms
     *
     * @return The raid room that the detector should startup in, or null for *all* rooms
     */
    public abstract RaidRoom getRaidRoom();

    /**
     * Detects mistakes for the given raider.
     * This is called during handling the {@link net.runelite.api.events.GameTick} event, each tick.
     *
     * @param raider - The raider to detect mistakes for
     * @return The list of {@link CoxMistake} detected on this tick
     */
    public abstract List<CoxMistake> detectMistakes(@NonNull Raider raider);

    /**
     * Detects mistakes for the team.
     * This is called during handling the {@link net.runelite.api.events.GameTick} event, each tick.
     * This is called after detecting mistakes for all individual raiders.
     *
     * @return The list of {@link CoxMistake} made by the team detected on this tick
     */
    public abstract List<CoxMistake> detectTeamMistakes();

    /**
     * This method allows detectors to handle some logic after all detectMistakes methods have been invoked
     * for this {@link net.runelite.api.events.GameTick}. Commonly, this is to cleanup state from after this tick.
     */
    public abstract void afterDetect();

    /**
     * Determines whether or not this detector is currently detecting mistakes. Commonly this is by checking the current
     * {@link RaidRoom} in the {@link RaidState}
     *
     * @return True if the detector is detecting mistakes, else false
     */
    public boolean isDetectingMistakes() {
        if (getRaidRoom() == null) { // null means *all* rooms
            return raidState.isInRaid();
        }

        return raidState.getCurrentRoom() == getRaidRoom();
    }

    protected WorldPoint getWorldPoint(Actor actor) {
        return WorldPoint.fromLocal(client, actor.getLocalLocation());
    }

    protected WorldPoint getWorldPoint(GraphicsObject graphicsObject) {
        return WorldPoint.fromLocal(client, graphicsObject.getLocation());
    }

    /**
     * This method computes the WorldPoints in a 3x3 area given a center point.
     *
     * @param center The center point of the 3x3 area
     * @return The set of WorldPoints around and including the center
     */
    protected Set<WorldPoint> compute3By3TilesFromCenter(WorldPoint center) {
        return computeNByNTilesFromCenter(center, 3);
    }

    /**
     * This method computes the WorldPoints in an NxN area given a center point.
     *
     * @param center The center point of the NxN area
     * @return The set of WorldPoints around and including the center
     */
    protected Set<WorldPoint> computeNByNTilesFromCenter(WorldPoint center, int N) {
        int partial = N / 2;
        Set<WorldPoint> locations = new HashSet<>();
        for (int i = -partial; i <= partial; i++) {
            for (int j = -partial; j <= partial; j++) {
                locations.add(center.dx(i).dy(j));
            }
        }
        return locations;
    }

    /**
     * Calculates and retrieves the activation tick for the specified GraphicsObject based on the start cycle and
     * the given hit delay.
     *
     * @param graphicsObject The graphics object
     * @param hitDelay       The delay in ticks from when the graphics object animation starts and when it causes a hit
     * @return The activation tick for when the graphics object will denote a hit on the player
     */
    protected int getActivationTick(GraphicsObject graphicsObject, int hitDelay) {
        int ticksToStart = (graphicsObject.getStartCycle() - client.getGameCycle()) / CYCLES_PER_GAME_TICK;
        return client.getTickCount() + ticksToStart + hitDelay; // Add the hit delay for how long between start to hit
    }

    /**
     * Calculates and retrieves the activation tick for the specified Projectile based on the remaining cycles.
     *
     * @param projectile The projectile object
     * @return The activation tick for when the projectile object will reach its target
     */
    protected int getActivationTick(Projectile projectile) {
        int ticksRemaining = projectile.getRemainingCycles() / CYCLES_PER_GAME_TICK;
        return client.getTickCount() + ticksRemaining;
    }
}

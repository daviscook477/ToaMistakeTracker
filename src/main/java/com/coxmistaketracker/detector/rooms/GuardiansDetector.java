package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.coxmistaketracker.detector.tracker.DelayedObjectsTracker;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Guardians hit with a falling boulder that make a graphics object when it hits so when the graphics object appears
 * check for players on the tile on the current tick. This is a mistake because with proper flinching
 * this attack will never hit.
 */
@Slf4j
@Singleton
public class GuardiansDetector extends BaseMistakeDetector {

    private static final int BOULDER_GRAPHICS_OBJECT_ID = 305;
    private static final int GUARDIANS_BOULDER_DELAY = 1;

    private final DelayedObjectsTracker<WorldPoint> delayedBoulderTilesTracker;


    public GuardiansDetector() {
        delayedBoulderTilesTracker = new DelayedObjectsTracker<>();
    }


    @Override
    public void cleanup() {
        delayedBoulderTilesTracker.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.GUARDIANS;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        for (WorldPoint boulderLocation : delayedBoulderTilesTracker.getActiveObjects()) {
            Set<WorldPoint> dangerousLocations = compute3By3TilesFromCenter(boulderLocation);
            if (dangerousLocations.contains(raider.getPreviousWorldLocation())) {
                mistakes.add(CoxMistake.GUARDIANS_BOULDER);
            }
        }

        return mistakes;
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        return ImmutableList.of();
    }

    @Override
    public void afterDetect() {}

    @Subscribe
    public void onGameTick(GameTick event) {
        delayedBoulderTilesTracker.onGameTick(client.getTickCount());
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == BOULDER_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            delayedBoulderTilesTracker.put(client.getTickCount() + GUARDIANS_BOULDER_DELAY, graphicsObjectPoint);
        }
    }
}

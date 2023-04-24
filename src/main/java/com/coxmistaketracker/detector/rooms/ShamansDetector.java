package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shamans hit with a green goo attack projectile that makes a graphics object when it hits so when the graphics object appears
 * check for players within the hit radius on the current tick.
 */
@Slf4j
@Singleton
public class ShamansDetector extends BaseMistakeDetector {

    private static final int GOO_GRAPHICS_OBJECT_ID = 1294;
    private static final int PROJECTILE_SIZE = 5;

    private final Set<WorldPoint> gooTiles;

    public ShamansDetector() {
        gooTiles = new HashSet<>();
    }


    @Override
    public void cleanup() {
        gooTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.SHAMANS;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (gooTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.SHAMANS_GOO);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        gooTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == GOO_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            gooTiles.addAll(computeNByNTilesFromCenter(graphicsObjectPoint, PROJECTILE_SIZE));
        }
    }
}

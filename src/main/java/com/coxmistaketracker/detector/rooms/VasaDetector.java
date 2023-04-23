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
 * Vasa hits with boulder projectiles that make a graphics object when they hit so when the graphics object appears
 * check for players within the hit radius on the current tick.
 */
@Slf4j
@Singleton
public class VasaDetector extends BaseMistakeDetector {
    private static final int BOULDER_GRAPHICS_OBJECT_ID = 1330;

    private final Set<WorldPoint> boulderTiles;


    public VasaDetector() {
        boulderTiles = new HashSet<>();
    }


    @Override
    public void cleanup() {
        boulderTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.VASA;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        // tanking hits at vasa is only a mistake if not venged
        if (boulderTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider)) {
            mistakes.add(CoxMistake.VASA_BOULDER);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        boulderTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == BOULDER_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            boulderTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        }
    }
}

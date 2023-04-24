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
 * Tekton hits with spark projectiles that make a graphics object when they hit so when the graphics object appears
 * check for players within the hit radius on the current tick.
 */
@Slf4j
@Singleton
public class TektonDetector extends BaseMistakeDetector {

    private static final int SPARK_GRAPHICS_OBJECT_ID = 659;

    private final Set<WorldPoint> sparkTiles;


    public TektonDetector() {
        sparkTiles = new HashSet<>();
    }


    @Override
    public void cleanup() {
        sparkTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.TEKTON;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (sparkTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.TEKTON_SPARKS);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        sparkTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == SPARK_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            sparkTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        }
    }
}

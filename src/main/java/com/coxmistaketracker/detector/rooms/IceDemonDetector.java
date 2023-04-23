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
 * Ice Demon hits with either a range projectile that make a graphics object it hits or
 * a mage attack that makes a graphics object when it hits.
 *
 * So when those graphics objects appear, all players (that are not venged) within the AOE of
 * the range gfx or on top of a mage gfx have made a mistake.
 */
@Slf4j
@Singleton
public class IceDemonDetector extends BaseMistakeDetector {

    private static final int RANGED_GRAPHICS_OBJECT_ID = 1325;
    private static final int MAGE_GRAPHICS_OBJECT_ID = 363;

    private final Set<WorldPoint> rangedTiles;
    private final Set<WorldPoint> mageTiles;


    public IceDemonDetector() {
        rangedTiles = new HashSet<>();
        mageTiles = new HashSet<>();
    }


    @Override
    public void cleanup() {
        rangedTiles.clear();
        mageTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.ICE_DEMON;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        // tanking hits in ice demon is only a mistake if not venged
        if (rangedTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider)) {
            mistakes.add(CoxMistake.ICE_DEMON_RANGED);
        }
        if (mageTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider)) {
            mistakes.add(CoxMistake.ICE_DEMON_MAGE);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        rangedTiles.clear();
        mageTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == RANGED_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            rangedTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        } else if (event.getGraphicsObject().getId() == MAGE_GRAPHICS_OBJECT_ID) {
            mageTiles.add(getWorldPoint(event.getGraphicsObject()));
        }
    }
}

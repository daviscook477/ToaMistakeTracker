package com.coxmistaketracker.detector.olm;

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
 * Being within range of a crystal bomb when it explodes is usually a mistake.
 * There are some exceptions: an example being when a bomb covers the entire range
 * of tiles from which the melee hand can be attacked and the player's attack cooldown
 * finishes on the same tick the bomb explodes - which means tanking the crystal bomb
 * is necessary to maintain dps. Additionally, the bomb can get in the way of the mage
 * skipper, especially if they are covered in acid - making tanking some of the bomb
 * damage reasonable.
 *
 * As a compromise, only the 3x3 centered on the bomb will be considered a mistake.
 * Since that's the most avoiable part and causes the player to take the most damage.
 */
@Slf4j
@Singleton
public class BombDetector extends BaseMistakeDetector {

    private static final int BOMB_EXPLOSION_GRAPHICS_OBJECT_ID = 40;

    private final Set<WorldPoint> bombExplodedTiles;

    public BombDetector() {
        bombExplodedTiles = new HashSet<>();
    }

    @Override
    public void cleanup() {
        bombExplodedTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        // the gfx appears on the tick the damage is applied so use the current location
        if (bombExplodedTiles.contains(raider.getCurrentWorldLocation())) {
            mistakes.add(CoxMistake.OLM_CRYSTAL_BOMB_EXPLOSION);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        bombExplodedTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == BOMB_EXPLOSION_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            bombExplodedTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        }
    }
}

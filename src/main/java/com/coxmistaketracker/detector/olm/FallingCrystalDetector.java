package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Falling crystals happen at olm in 2 ways:
 * 1. in between phases, crystals will fall all across the screen and all players must dodge them
 * 2. as an attack, the olm will target one player with falling crystals that deal damage to all players
 *    under it when it finishes falling.
 *
 * These attacks use different graphics ids, so we can differentiate between these as mistakes.
 * This is important because the falling crystals between phases hit in a radius of 2, whereas the attack
 * only hits on the targetted tile.
 *
 * While I think similar reasoning to that of the acid pool damage could be used to justify not using a chat message
 * for the falling crystal attack, since knowing that you're taking damage from the attack allows the player to react to
 * it and stop taking damage (since like the acid pool damage, the attack continues over time), I think the falling
 * crystal attack is already made so obvious in game by the red circle around the players feet, the giant falling crystal
 * graphic, and the huge damage splats, that adding a chat message too won't meaningfully assist players in tackling
 * this game mechanic.
 */
public class FallingCrystalDetector extends BaseMistakeDetector {

    // for some reason I'm pretty sure I've seen both of these ids used
    private static final Set<Integer> FALLING_CRYSTAL_BETWEEN_PHASES_GRAPHICS_OBJECT_IDS = ImmutableSet.of(1357, 1358);
    private static final int FALLING_CRYSTAL_ATTACK_GRAPHICS_OBJECT_ID = 1353;

    private final Set<WorldPoint> betweenPhasesTiles;
    private final Set<WorldPoint> attackTiles;


    public FallingCrystalDetector() {
        betweenPhasesTiles = new HashSet<>();
        attackTiles = new HashSet<>();
    }


    @Override
    public void cleanup() {
        betweenPhasesTiles.clear();
        attackTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (betweenPhasesTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.OLM_FALLING_CRYSTAL_BETWEEN_PHASES);
        }
        if (attackTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.OLM_FALLING_CRYSTAL_ATTACK);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        betweenPhasesTiles.clear();
        attackTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (FALLING_CRYSTAL_BETWEEN_PHASES_GRAPHICS_OBJECT_IDS.contains(event.getGraphicsObject().getId())) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            betweenPhasesTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        } else if (event.getGraphicsObject().getId() == FALLING_CRYSTAL_ATTACK_GRAPHICS_OBJECT_ID) {
            attackTiles.add(getWorldPoint(event.getGraphicsObject()));
        }
    }
}

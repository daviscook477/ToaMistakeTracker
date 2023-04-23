package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The olm has 4 special attacks that can be used and with the strategy of skipping specials, are
 * arguably mistakes when they happen (in most scenarios).
 *
 * During all hand phases prior to the last hand phase, any of the 3 special
 * attacks (crystals, lightning, and teleports) that can occur are mistakes while
 * both hands are alive. The team should either be mage skipping or melee skipping.
 * When one hand dies and the other one remains (usually the mage hand dies), the team will pile
 * the remaining hand which will keep the olm facing one side in perpetuity and have specials occur.
 * As such, when a special occurs, it only counts as a mistake if there are both hands are alive.
 *
 * In the last hand phase, both hands must be killed at the same time, so teams will allow some
 * specials to occur in order to facilitate that. This means that specials are only conditionally a
 * mistake in this phase (when the team is getting the mage hand HP down is when specials are a mistake),
 * but I don't want to write code for checking that condition so instead specials will not be considered
 * a mistake in this phase. This requires knowing which phase of the fight we are on. Olm has glowing
 * eyes in this phase, so we detect that change in (animation id, npc id - what changes?) in order to
 * skip counting these as mistakes.
 */
@Slf4j
@Singleton
public class SpecialDetector extends BaseMistakeDetector {

    private static final int CRYSTALS_GAME_OBJECT_ID = 30033;
    private static final int LIGHTNING_GRAPHICS_OBJECT_ID = 1356;
    private static final int TELEPORTS_GRAPHICS_OBJECT_ID = 1359;
    private static final int CRYSTALS_EXPLODED_GRAPHICS_OBJECT_ID = 1338;

    /**
     * This needs to be long enough to cover the time beteween teleports starting and teleports ending, but not too long
     * that another teleports special could occur within this time frame of the first one.
     */
    private static final int TELEPORTS_COOLDOWN_TICKS = 20;

    private final Set<WorldPoint> crystalsExplodedTiles;
    private final Set<WorldPoint> lightningDangerTiles;


    private boolean crystals;
    private boolean lightning;
    private boolean lastLightning;
    private boolean currentLightning;
    private boolean teleports;
    private int lastTeleportsTick;

    public SpecialDetector() {
        crystals = false;
        lightning = false;
        lastLightning = false;
        currentLightning = false;
        teleports = false;
        lastTeleportsTick = 0;
        crystalsExplodedTiles = new HashSet<>();
        lightningDangerTiles = new HashSet<>();
    }

    @Override
    public void cleanup() {
        crystals = false;
        lightning = false;
        lastLightning = false;
        currentLightning = false;
        teleports = false;
        lastTeleportsTick = 0;
        crystalsExplodedTiles.clear();
        lightningDangerTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (crystals) {
            mistakes.add(CoxMistake.OLM_SPECIAL_CRYSTAL_OCCURS);
        }
        if (lightning) {
            mistakes.add(CoxMistake.OLM_SPECIAL_LIGHTNING_OCCURS);
        }
        if (teleports) {
            mistakes.add(CoxMistake.OLM_SPECIAL_TELEPORTS_OCCURS);
        }

        if (crystalsExplodedTiles.size() > 0) {
            log.debug("Raider " + raider.getName() + " was at position " + raider.getPreviousWorldLocation() + " but the crystal explosion was on " + crystalsExplodedTiles.toArray()[0]);
        }
        if (crystalsExplodedTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.OLM_SPECIAL_CRYSTALS_DAMAGE);
        }
        if (lightningDangerTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(CoxMistake.OLM_SPECIAL_LIGHTNING_DAMAGE);
        }
        // HACK
        // Normally a player cannot move > 2 tiles in 1 tick. Although teleports can teleport the player 1 or 2 tiles, I haven't figured out a way
        // to uniquely detect that so instead only trigger this mistake when the player moves > 2 tiles in a tick which is definitely a teleport

        WorldPoint currentLocation = raider.getCurrentWorldLocation();
        WorldPoint previousLocation = raider.getPreviousWorldLocation();
        if (currentLocation != null && previousLocation != null && (Math.abs(raider.getCurrentWorldLocation().getX() - raider.getPreviousWorldLocation().getX()) > 2 || Math.abs(raider.getCurrentWorldLocation().getY() - raider.getPreviousWorldLocation().getY()) > 2)) {
            mistakes.add(CoxMistake.OLM_SPECIAL_TELEPORTS_DAMAGE);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        crystals = false;
        lastLightning = currentLightning;
        currentLightning = false;
        lightning = false;
        teleports = false;
        crystalsExplodedTiles.clear();
        lightningDangerTiles.clear();
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == CRYSTALS_GAME_OBJECT_ID) {
            crystals = true;
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        int currentTeleportsTick = client.getTickCount();
        // leading edge detector - only counts when going from no lightning to lightning
        if (event.getGraphicsObject().getId() == LIGHTNING_GRAPHICS_OBJECT_ID) {
            currentLightning = true;
            if (!lastLightning) {
                lightning = true;
                lightningDangerTiles.add(getWorldPoint(event.getGraphicsObject()));
            }
        } else if (event.getGraphicsObject().getId() == TELEPORTS_GRAPHICS_OBJECT_ID && (currentTeleportsTick - lastTeleportsTick) >= TELEPORTS_COOLDOWN_TICKS) {
            teleports = true;
            lastTeleportsTick = currentTeleportsTick;
        } else if (event.getGraphicsObject().getId() == CRYSTALS_EXPLODED_GRAPHICS_OBJECT_ID) {
            crystalsExplodedTiles.add(getWorldPoint(event.getGraphicsObject()));
        }
    }


    private boolean areSpecialsMistakes() {
        return true;
    }
}

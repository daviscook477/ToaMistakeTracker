package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.HitsplatID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.*;

/**
 * Ice Demon hits with either a range projectile that make a graphics object it hits or
 * a mage attack that makes a graphics object when it hits.
 *
 * So when those graphics objects appear, all players (that are not venged) within the AOE of
 * the range gfx or on top of a mage gfx have made a mistake.
 *
 * There is a complication though, since we can only detect whether a player is venged or not by whether the
 * "Taste vengeance!" message pops up in chat. This will only show if the player gets hit a non-zero amount of damage.
 *
 * It would be incorrect to track 0 hits on players that are venged as mistakes, so to avoid this scenario, any hit
 * for a zero will not be considered a mistake despite the fact that if the player was not venged it's just a mistake
 * that went unpunished.
 */
@Slf4j
@Singleton
public class IceDemonDetector extends BaseMistakeDetector {

    private static final int RANGED_GRAPHICS_OBJECT_ID = 1325;
    private static final int MAGE_GRAPHICS_OBJECT_ID = 363;

    private final Set<WorldPoint> rangedTiles;
    private final Set<WorldPoint> mageTiles;

    // name -> list of hitsplat amounts
    private final Map<String, List<Integer>> appliedHitsplats;


    public IceDemonDetector() {
        rangedTiles = new HashSet<>();
        mageTiles = new HashSet<>();
        appliedHitsplats = new HashMap<>();
    }


    @Override
    public void cleanup() {
        rangedTiles.clear();
        mageTiles.clear();
        appliedHitsplats.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.ICE_DEMON;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        // tanking hits in ice demon is only a mistake if not venged
        if (rangedTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider) && raiderHasNonZeroHitsplat(raider)) {
            mistakes.add(CoxMistake.ICE_DEMON_RANGED);
        }
        if (mageTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider) && raiderHasNonZeroHitsplat(raider)) {
            mistakes.add(CoxMistake.ICE_DEMON_MAGE);
        }

        return mistakes;
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        return ImmutableList.of();
    }

    private boolean raiderHasNonZeroHitsplat(Raider raider) {
        if (!appliedHitsplats.containsKey(raider.getName())) return false;
        List<Integer> hitsplats = appliedHitsplats.get(raider.getName());
        if (hitsplats.isEmpty()) return false;
        int hitsplatAmount = 0;
        for (int hitsplat : hitsplats) {
            hitsplatAmount += hitsplat;
        }
        return hitsplatAmount > 0;
    }

    @Override
    public void afterDetect() {
        rangedTiles.clear();
        mageTiles.clear();
        appliedHitsplats.clear();
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == null || event.getActor().getName() == null) return;

        String name = Text.removeTags(event.getActor().getName());
        if (raidState.isRaider(event.getActor())) {
            if (isDamageHitsplat(event.getHitsplat().getHitsplatType()) &&
                    event.getHitsplat().getAmount() > 0) {
                appliedHitsplats.computeIfAbsent(name, k -> new ArrayList<>())
                        .add(event.getHitsplat().getAmount());
            }
        }
    }

    private boolean isDamageHitsplat(int hitsplatType) {
        return hitsplatType == HitsplatID.DAMAGE_ME || hitsplatType == HitsplatID.DAMAGE_OTHER;
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

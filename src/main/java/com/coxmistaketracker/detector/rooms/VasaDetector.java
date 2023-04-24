package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
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
 * Vasa hits with boulder projectiles that make a graphics object when they hit so when the graphics object appears
 * check for players within the hit radius on the current tick.
 *
 * So when those graphics objects appear, all players (that are not venged) within the AOE of
 * the boulder gfx have made a mistake.
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
public class VasaDetector extends BaseMistakeDetector {
    private static final int BOULDER_GRAPHICS_OBJECT_ID = 1330;

    private final Set<WorldPoint> boulderTiles;
    // name -> list of hitsplat amounts
    private final Map<String, List<Integer>> appliedHitsplats;


    public VasaDetector() {
        boulderTiles = new HashSet<>();
        appliedHitsplats = new HashMap<>();
    }


    @Override
    public void cleanup() {
        boulderTiles.clear();
        appliedHitsplats.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.VASA;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        // tanking hits at vasa is only a mistake if not venged
        if (boulderTiles.contains(raider.getPreviousWorldLocation()) && !vengeanceTracker.didPopVengeance(raider) && raiderHasNonZeroHitsplat(raider)) {
            mistakes.add(CoxMistake.VASA_BOULDER);
        }

        return mistakes;
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
        boulderTiles.clear();
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
        if (event.getGraphicsObject().getId() == BOULDER_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            boulderTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        }
    }
}

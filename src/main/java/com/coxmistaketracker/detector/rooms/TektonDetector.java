package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.*;

/**
 * Tekton hits with spark projectiles that make a graphics object when they hit so when the graphics object appears
 * check for players within the hit radius on the current tick.
 *
 * Tekton's melee attack is dodgeable by either running around tekton or flinching tekton. Getting hit by the melee
 * attack without vengeance up is a mistake.
 */
@Slf4j
@Singleton
public class TektonDetector extends BaseMistakeDetector {

    private static final int SPARK_GRAPHICS_OBJECT_ID = 659;
    private static final Set<Integer> TEKTON_IDS = ImmutableSet.of(7540, 7541, 7542, 7543, 7544, 7545);
    private static final Set<Integer> TEKTON_ATTACK_ANIMATION_IDS = ImmutableSet.of(7492, 7493, 7494);

    private final Set<WorldPoint> sparkTiles;
    // name -> list of hitsplat amounts
    private final Map<String, List<Integer>> appliedHitsplats;
    private NPC tekton;

    public TektonDetector() {
        sparkTiles = new HashSet<>();
        appliedHitsplats = new HashMap<>();
        tekton = null;
    }


    @Override
    public void cleanup() {
        sparkTiles.clear();
        appliedHitsplats.clear();
        tekton = null;
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

        if (!vengeanceTracker.didPopVengeance(raider) && raiderHasNonZeroHitsplat(raider)) {
            mistakes.add(CoxMistake.TEKTON_MELEE);
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
        sparkTiles.clear();
        appliedHitsplats.clear();
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null) return;

        if (TEKTON_IDS.contains(event.getNpc().getId())) {
            tekton = event.getNpc();
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc() == null) return;

        if (TEKTON_IDS.contains(event.getNpc().getId())) {
            tekton = null;
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == null || event.getActor().getName() == null) return;

        String name = Text.removeTags(event.getActor().getName());
        if (raidState.isRaider(event.getActor())) {
            if (isDamageHitsplat(event.getHitsplat().getHitsplatType()) &&
                    event.getHitsplat().getAmount() > 0) {
                if (tekton != null && TEKTON_ATTACK_ANIMATION_IDS.contains(tekton.getAnimation())) {
                    appliedHitsplats.computeIfAbsent(name, k -> new ArrayList<>())
                            .add(event.getHitsplat().getAmount());
                }
            }
        }
    }

    private boolean isDamageHitsplat(int hitsplatType) {
        return hitsplatType == HitsplatID.DAMAGE_ME || hitsplatType == HitsplatID.DAMAGE_OTHER;
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == SPARK_GRAPHICS_OBJECT_ID) {
            WorldPoint graphicsObjectPoint = getWorldPoint(event.getGraphicsObject());
            sparkTiles.addAll(compute3By3TilesFromCenter(graphicsObjectPoint));
        }
    }
}

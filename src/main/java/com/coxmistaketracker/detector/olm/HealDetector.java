package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.coxmistaketracker.detector.tracker.AppliedHitsplatsTracker;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import java.util.*;

/**
 * When the melee hand is using healing special, any damage dealt to it heals it instead.
 * This can be detected by looking for the combination of a healing hitsplat appearing on the melee hand
 * and a player starting an animation interacting with the melee hand on the same tick.
 *
 * When the hands aren't killed at the same time during the last hand phase, they can reset.
 * This can be detected by looking for one of the hands spawning while the head is already spawned.
 */
@RequiredArgsConstructor
@Slf4j
public class HealDetector extends BaseMistakeDetector {

    private final static Set<Integer> OLM_IDS = ImmutableSet.of(7551, 7554);
    private final static Set<Integer> RIGHT_CLAW_IDS = ImmutableSet.of(7550, 7553);
    private final static Set<Integer> LEFT_CLAW_IDS = ImmutableSet.of(7552, 7555);
    private final static Set<Integer> OLM_PARTS_IDS = new HashSet<>();

    static {
        OLM_PARTS_IDS.addAll(OLM_IDS);
        OLM_PARTS_IDS.addAll(RIGHT_CLAW_IDS);
        OLM_PARTS_IDS.addAll(LEFT_CLAW_IDS);
    }

    private final static String LEFT_CLAW_NAME = "Left Claw";

    private final AppliedHitsplatsTracker appliedHitsplatsTracker;
    private final Set<String> interactingWithHand;
    private final Set<Integer> spawnedOlmParts;

    public HealDetector() {
        appliedHitsplatsTracker = new AppliedHitsplatsTracker();
        interactingWithHand = new HashSet<>();
        spawnedOlmParts = new HashSet<>();
    }

    @Override
    public void cleanup() {
        appliedHitsplatsTracker.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    private <T> boolean intersects(Set<T> a, Set<T> b) {
        Set<T> c = new HashSet<>(a);
        c.retainAll(b);
        return c.size() > 0;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (interactingWithHand.contains(raider.getName()) && appliedHitsplatsTracker.peekHitsplatApplied(LEFT_CLAW_NAME)) {
            mistakes.add(CoxMistake.OLM_LEFT_CLAW_HEAL);
        }

        if (!intersects(spawnedOlmParts, OLM_IDS) && (intersects(spawnedOlmParts, RIGHT_CLAW_IDS) || intersects(spawnedOlmParts, LEFT_CLAW_IDS))) {
            mistakes.add(CoxMistake.OLM_CLAW_RESET);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        appliedHitsplatsTracker.clear();
        interactingWithHand.clear();
        spawnedOlmParts.clear();
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == null) return;

        if (event.getActor() instanceof NPC && LEFT_CLAW_IDS.contains(((NPC) event.getActor()).getId()) && event.getHitsplat().getHitsplatType() == HitsplatID.HEAL) {
            appliedHitsplatsTracker.addHitsplatForRaider(LEFT_CLAW_NAME);
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() == null || event.getActor().getName() == null) return;

        String name = Text.removeTags(event.getActor().getName());
        if (event.getActor() instanceof Player && raidState.isRaider(event.getActor())) {
            Actor interacting = event.getActor().getInteracting();

            if (interacting instanceof NPC && LEFT_CLAW_IDS.contains(((NPC) interacting).getId())) {
                interactingWithHand.add(name);
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null) return;

        int id = event.getNpc().getId();
        if (OLM_PARTS_IDS.contains(id)) {
            spawnedOlmParts.add(id);
        }
    }

}

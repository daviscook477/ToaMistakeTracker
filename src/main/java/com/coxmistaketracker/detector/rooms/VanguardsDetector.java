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
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Causing the vanguards to heal back to full HP by not keeping their HP sufficiently close to each other is a mistake.
 */
@Slf4j
@Singleton
public class VanguardsDetector extends BaseMistakeDetector {

    private final Set<Integer> VANGUARDS_IDS = ImmutableSet.of(7525, 7526, 7527, 7528, 7529);
    private boolean reset;

    public VanguardsDetector() {
        reset = false;
    }

    @Override
    public void cleanup() {
        reset = false;
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.VANGUARDS;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        return ImmutableList.of();
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (reset) {
            mistakes.add(CoxMistake.VANGUARDS_RESET);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        reset = false;
    }


    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == null) return;

        if (event.getActor() instanceof NPC && VANGUARDS_IDS.contains(((NPC) event.getActor()).getId())
                && event.getHitsplat().getHitsplatType() == HitsplatID.HEAL) {
            reset = true;
        }
    }
}

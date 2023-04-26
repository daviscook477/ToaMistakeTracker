package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * I don't know everything there is to know about Chambers of Xeric, but I'm fairly sure that outside
 * of solo chambers, there isn't ever a reason to center the head - meaning that any time it turns center
 * the team has made a mistake and more players than necessary are tanking olm auto attacks. It is
 * possible to center the olm head in small scales (less than N players) if everyone attacking the
 * mage hand splashes without it really being a genuine mistake, so I might have to disable this mistake for
 * less than N players not just solo chambers (where turning middle is part of the strategy).
 *
 * In the team size where this is a mistake, it can be detected by looking at the olm's animations.
 * There is a different animation played for centering the head compared to the animation played for making a full
 * turn from left to right and vice versa so this code just looks for that animation in particular and counts it
 * as a mistake for the team.
 */
@Slf4j
@Singleton
public class CenterDetector extends BaseMistakeDetector {

    private final static Set<Integer> CENTER_ANIMATION_IDS = ImmutableSet.of(7342, 7340);
    private final static Set<Integer> OLM_IDS = ImmutableSet.of(7551, 7554);

    private boolean centered;

    public CenterDetector() {
        centered = false;
    }

    @Override
    public void cleanup() {
        centered = false;
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        return ImmutableList.of();
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        List<CoxMistake> mistakes = new ArrayList<>();

        // this is only a mistake in team raids
        if (centered && !raidState.isSoloRaid()) {
            mistakes.add(CoxMistake.OLM_CENTER);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        centered = false;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() == null) return;

        if (event.getActor() instanceof NPC && OLM_IDS.contains(((NPC) event.getActor()).getId()) && CENTER_ANIMATION_IDS.contains(event.getActor().getAnimation())) {
            centered = true;
        }
    }

}

package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.coxmistaketracker.detector.tracker.OverheadTracker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.HeadIcon;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The prayer orbs are projectiles that check for overhead prayer correctness when they hit, so we
 * can use the {@link OverheadTracker} to verify whether the correct overhead was prayed on the
 * hit tick.
 */
@Slf4j
@Singleton
public class PrayerOrbDetector extends BaseMistakeDetector {

    private static final Map<Integer, HeadIcon> SPECIAL_PRAYER_ATTACKS = ImmutableMap.of(
            1341, HeadIcon.MAGIC,
            1343, HeadIcon.RANGED,
            1345, HeadIcon.MELEE
    );

    private final OverheadTracker specialPrayerOverheadTracker;

    public PrayerOrbDetector() {
        specialPrayerOverheadTracker = new OverheadTracker(SPECIAL_PRAYER_ATTACKS);
    }

    @Override
    public void cleanup() {
        specialPrayerOverheadTracker.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (specialPrayerOverheadTracker.didMissPrayer(raider)) {
            mistakes.add(CoxMistake.OLM_PRAYER_ORB);
        }

        return mistakes;
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        return ImmutableList.of();
    }

    @Override
    public void afterDetect() {}


    @Subscribe
    public void onGameTick(GameTick event) {
        specialPrayerOverheadTracker.onGameTick(client.getTickCount());
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (!SPECIAL_PRAYER_ATTACKS.containsKey(event.getProjectile().getId())) return;

        specialPrayerOverheadTracker.trackProjectile(event, getActivationTick(event.getProjectile()));
    }

}

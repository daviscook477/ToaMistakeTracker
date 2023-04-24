package com.coxmistaketracker.detector.rooms;

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

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lux grubs hatching gets in the way of the redemption method of finishing off the abyssal portal (I think so at least).
 * So if one hatches, it is a mistake.
 */
@Slf4j
@Singleton
public class VespulaDetector extends BaseMistakeDetector {

    private final static Set<Integer> LUX_GRUB_IDS = ImmutableSet.of(7534, 7535, 7536, 7537);
    private final static int LUX_GRUB_HATCH_ANIMATION_ID = 7466;
    private boolean hatched;

    public VespulaDetector() {
        hatched = false;
    }


    @Override
    public void cleanup() {
        hatched = false;
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.VESPULA;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        return ImmutableList.of();
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (hatched) {
            mistakes.add(CoxMistake.VESPULA_LUX_GRUB_HATCHED);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        hatched = false;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() == null || !(event.getActor() instanceof NPC)) return;

        int npcId = ((NPC) event.getActor()).getId();
        if (LUX_GRUB_IDS.contains(npcId) && event.getActor().getAnimation() == LUX_GRUB_HATCH_ANIMATION_ID) {
            hatched = true;
        }
    }
}

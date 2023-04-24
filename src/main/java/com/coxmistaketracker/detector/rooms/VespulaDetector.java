package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.coxmistaketracker.events.RaidRoomChanged;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.NpcSpawned;
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

    private final static Set<Integer> VESPINE_SOLIDER_IDS = ImmutableSet.of(7538, 7539);
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
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null) return;

        int npcId = event.getNpc().getId();
        if (VESPINE_SOLIDER_IDS.contains(npcId)) {
            hatched = true;
        }
    }
}

package com.coxmistaketracker.detector.rooms;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Muttadiles will eat at the meat tree and heal if the player get them below 40% HP without chopping the tree or
 * successfully freezing the muttadile. While it is possible to intentionally come into a raid intending to neither
 * chop the tree or freeze the muttadiles, this is an unnecessary time loss so there's no reason to not count this.
 */
@Slf4j
@Singleton
public class MuttadilesDetector extends BaseMistakeDetector {

    private final static int SMALL_MUTTADILE_ID = 7562;
    private final static int BIG_MUTTADILE_ID = 7563;

    /**
     * This needs to be long enough to over the time between the healing starting and ending, but not too long
     * that the muttadile could start healing again within the window.
     *
     * This is hacky but probably close enough.
     */
    private static final int HEAL_COOLDOWN_TICKS = 10;

    private boolean smallHealed;
    private boolean bigHealed;

    private int lastHealTick;

    public MuttadilesDetector() {
        smallHealed = false;
        bigHealed = false;
        lastHealTick = 0;
    }


    @Override
    public void cleanup() {
        smallHealed = false;
        bigHealed = false;
        lastHealTick = 0;
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.MUTTADILES;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (smallHealed) {
            mistakes.add(CoxMistake.SMALL_MUTTADILE_HEAL);
        }
        if (bigHealed) {
            mistakes.add(CoxMistake.BIG_MUTTADILE_HEAL);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        smallHealed = false;
        bigHealed = false;
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == null) return;

        int currentHealTick = client.getTickCount();
        if (event.getActor() instanceof NPC && SMALL_MUTTADILE_ID == ((NPC) event.getActor()).getId()
        && event.getHitsplat().getHitsplatType() == HitsplatID.HEAL
        && (currentHealTick - lastHealTick) >= HEAL_COOLDOWN_TICKS) {
            smallHealed = true;
            lastHealTick = currentHealTick;
        } else if (event.getActor() instanceof NPC && BIG_MUTTADILE_ID == ((NPC) event.getActor()).getId()
        && event.getHitsplat().getHitsplatType() == HitsplatID.HEAL
        && (currentHealTick - lastHealTick) >= HEAL_COOLDOWN_TICKS) {
            bigHealed = true;
            lastHealTick = currentHealTick;
        }
    }
}

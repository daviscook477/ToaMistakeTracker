package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.List;

/**
 * Olm throws a flame wall that may trap a player. If a player is in the flame wall when it disappears
 * they take damage that is otherwise avoidable.
 */
@Slf4j
@Singleton
public class FlameWallDetector extends BaseMistakeDetector {

    private static final int FLAME_WALL_ID = 7558;


    @Override
    public void cleanup() {

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
        return ImmutableList.of();
    }

    @Override
    public void afterDetect() {

    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() != null && event.getNpc().getId() == FLAME_WALL_ID) {
            log.debug("found flame wall at y=" + event.getNpc().getWorldLocation().getY() + " on tick " + client.getTickCount());
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc() != null && event.getNpc().getId() == FLAME_WALL_ID) {
            log.debug("found flame wall despawn on tick " + client.getTickCount());
        }
    }
}

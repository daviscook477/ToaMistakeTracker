package com.coxmistaketracker.detector.death;

import com.google.common.collect.ImmutableMap;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.coxmistaketracker.CoxMistake.*;

/**
 * Track deaths for each player in the raid using the {@link ActorDeath} event. Also map each room to the room death.
 */
@Slf4j
@Singleton
public class DeathDetector extends BaseMistakeDetector {

    private static final Map<RaidRoom, CoxMistake> ROOM_DEATHS = ImmutableMap.<RaidRoom, CoxMistake>builder()
            .put(RaidRoom.ICE_DEMON, DEATH_ICE_DEMON)
            .put(RaidRoom.SHAMANS, DEATH_SHAMANS)
            .put(RaidRoom.MUTTADILES, DEATH_MUTTADILES)
            .put(RaidRoom.MYSTICS, DEATH_MYSTICS)
            .put(RaidRoom.TEKTON, DEATH_TEKTON)
            .put(RaidRoom.VANGUARDS, DEATH_VANGUARDS)
            .put(RaidRoom.VASA, DEATH_VASA)
            .put(RaidRoom.VESPULA, DEATH_VESPULA)
            .put(RaidRoom.GUARDIANS, DEATH_GUARDIANS)
            .put(RaidRoom.TIGHTROPE, DEATH_TIGHTROPE)
            .put(RaidRoom.OLM, DEATH_OLM)
            .build();

    private final Set<String> raiderDeaths;

    public DeathDetector() {
        raiderDeaths = new HashSet<>();
    }

    @Override
    public void cleanup() {
        raiderDeaths.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return null; // null means *all* rooms
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (raiderDeaths.contains(raider.getName())) {
            mistakes.add(DEATH);

            if (!ROOM_DEATHS.containsKey(raidState.getCurrentRoom())) {
                // Should never happen. If it does, log and return empty mistakes for this death
                log.error("Unknown room death: {}", raidState.getCurrentRoom());
                return Collections.emptyList();
            }

            mistakes.add(ROOM_DEATHS.get(raidState.getCurrentRoom()));
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        raiderDeaths.clear();
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (!(event.getActor() instanceof Player)) return;

        if (raidState.isRaider(event.getActor()))
            raiderDeaths.add(event.getActor().getName());
    }
}
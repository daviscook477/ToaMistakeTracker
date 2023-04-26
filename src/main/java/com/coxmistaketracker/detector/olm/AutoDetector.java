package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.HeadIcon;
import net.runelite.api.Projectile;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.*;

import static com.coxmistaketracker.CoxMistakeTrackerPlugin.CYCLES_PER_GAME_TICK;

/**
 * Olm's auto attacks cannot be entirely predicted, but they do have a pattern. In particular, the olm
 * is more likely to attack with the same style that it just attacked with than it is to swap styles.
 *
 * As such, the "correct" overhead prayer to use is always that which would protect against the olm's
 * most recent auto (assuming the player is not venged and it is not head phase).
 *
 * Since the "punish" for praying incorrectly isn't over the first time the player tanks an olm hit off prayer,
 * showing a message over the player's head when this mistake is made could allow the player to react faster than
 * just the auto alone (especially in the scenario where the player tanks the hit as a 0 despite having the wrong prayer up).
 * So this mistake has no shown message above and it just available to look at in the sidebar after the raid.
 */
@Slf4j
@Singleton
public class AutoDetector extends BaseMistakeDetector {

    private static final Set<Integer> GREAT_OLM_IDS = ImmutableSet.of(7551, 75534);
    private static final int AUTO_MAGE_ID = 1339;
    private static final int AUTO_RANGED_ID = 1340;
    private static final Map<Integer, HeadIcon> REQUIRED_PROTECTION = ImmutableMap.of(AUTO_MAGE_ID, HeadIcon.MAGIC, AUTO_RANGED_ID, HeadIcon.RANGED);
    private static final Set<HeadIcon> VALID_PROTECTION = ImmutableSet.of(HeadIcon.RANGED, HeadIcon.MAGIC);
    // olm projectiles don't last longer than 10 ticks
    private static final int TICKS_ALIVE = 10;

    private int lastAutoId;
    private int currentAutoId;
    private final List<Projectile> projectiles;
    private final Set<Projectile> countedAlready;

    private final Map<Projectile, Integer> tickToRemoveMap;

    public AutoDetector() {
        lastAutoId = -1;
        currentAutoId = -1;
        projectiles = new ArrayList<>();
        countedAlready = new HashSet<>();
        tickToRemoveMap = new HashMap<>();
    }

    @Override
    public void cleanup() {
        lastAutoId = -1;
        currentAutoId = -1;
        projectiles.clear();
        countedAlready.clear();
        tickToRemoveMap.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            if (raider.getPlayer().equals(projectile.getInteracting())) {
                HeadIcon actual = raider.getPlayer().getOverheadIcon();
                // not having up either of pray range or mage is a mistake regardless of prior state
                if (!VALID_PROTECTION.contains(actual)) {
                    mistakes.add(CoxMistake.OLM_AUTO);
                    continue;
                }

                if (lastAutoId > 0) {
                    // mistakes are based on whether the current overhead matches the previous attack
                    // doesn't matter whether the current attack is prayed against correctly
                    HeadIcon expected = REQUIRED_PROTECTION.get(lastAutoId);
                    if (expected == null) {
                        // shouldn't happen, but in just in case, skip
                        continue;
                    }
                    if (actual != expected) {
                        mistakes.add(CoxMistake.OLM_AUTO);
                    }
                    log.info("playerHeadIcon for player " + raider.getName() + " had icon " + actual.name() + " but required " + expected.name() + " for projectile " + projectile.getId() + " w/ remaining " + projectile.getRemainingCycles() + " cycles");
                }
            }
        }

        return mistakes;
    }

    @Override
    public List<CoxMistake> detectTeamMistakes() {
        return ImmutableList.of();
    }

    @Override
    public void afterDetect() {
        countedAlready.addAll(projectiles);
        projectiles.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // cleanup projectiles from the tracking map once they've outlived
        // the duration for which they might receive ProjectileMoved events
        int currentTick = client.getTickCount();
        List<Projectile> toRemove = new ArrayList<>();
        for (Map.Entry<Projectile, Integer> entry : tickToRemoveMap.entrySet()) {
            if (currentTick >= entry.getValue()) {
                toRemove.add(entry.getKey());
            }
        }
        for (Projectile projectile : toRemove) {
            countedAlready.remove(projectile);
            tickToRemoveMap.remove(projectile);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() != null && GREAT_OLM_IDS.contains(event.getNpc().getId())) {
            log.debug("Spawned the olm - resetting auto information");
            lastAutoId = -1;
            currentAutoId = -1;
            projectiles.clear();
            countedAlready.clear();
            tickToRemoveMap.clear();
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (event.getProjectile() != null && (event.getProjectile().getId() == AUTO_MAGE_ID || event.getProjectile().getId() == AUTO_RANGED_ID) && !countedAlready.contains(event.getProjectile())) {
            lastAutoId = currentAutoId;
            currentAutoId = event.getProjectile().getId();
            projectiles.add(event.getProjectile());
            tickToRemoveMap.put(event.getProjectile(), client.getTickCount() + TICKS_ALIVE);
        }
    }
}

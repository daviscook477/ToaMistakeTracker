package com.coxmistaketracker.detector.olm;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidRoom;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.BaseMistakeDetector;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Olm acid pools will appear under the players current position, and in order to attack the olm, the player must
 * stand still for a tick (unless it is a drag attack) which can cause the player to tank acid damage without it
 * being a mistake (losing DPS for the entirey of the "covered in acid" attack would be a much bigger mistake than
 * tanking acid for 1 tick every time your attack comes off cooldown) so the mistake has to be standing in an acid pool
 * for more than 1 tick (similar to how the "perfect olm" combat achievement works).
 *
 * Since the "punish" for standing in acid isn't over the first tick the player stays in a pool for more than 1 tick,
 * showing a message over the player's head when this mistake is made could allow the player to react faster to the acid
 * damage than just the poison hitsplats alone so there is no message shown for this mistake - it is only tracked in
 * the sidebar. I don't think it's realistic to be concerned about players monitoring their sidebar for mistakes since
 * it would be many times harder to notice a mistake by watching the sidebar than by just paying attention to the game.
 */
@Slf4j
@Singleton
public class AcidDetector extends BaseMistakeDetector {

    private final static int ACID_TILE_GAME_OBJECT_ID = 30032;

    private final Set<WorldPoint> acidTiles;

    public AcidDetector() {
        acidTiles = new HashSet<>();
    }

    @Override
    public void cleanup() {
        acidTiles.clear();
    }

    @Override
    public RaidRoom getRaidRoom() {
        return RaidRoom.OLM;
    }

    @Override
    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = new ArrayList<>();

        if (acidTiles.contains(raider.getPreviousWorldLocation()) && raider.getPreviousWorldLocation().equals(raider.getCurrentWorldLocation())) {
            mistakes.add(CoxMistake.OLM_ACID_STANDSTILL);
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
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == ACID_TILE_GAME_OBJECT_ID) {
            acidTiles.add(event.getGameObject().getWorldLocation());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject().getId() == ACID_TILE_GAME_OBJECT_ID) {
            acidTiles.remove(event.getGameObject().getWorldLocation());
        }
    }

}

package com.coxmistaketracker;

import com.coxmistaketracker.events.InRaidChanged;
import com.coxmistaketracker.events.RaidEntered;
import com.coxmistaketracker.events.RaidRoomChanged;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RaidState {

    public static final Set<Integer> COX_REGION_IDS = ImmutableSet.of(13136, 13137, 13138, 13139, 13140, 13141, 13145, 13393, 13394, 13395, 13396, 13397, 13401, 12889);
    public static final int OLM_REGION_ID = 12889;
    private static final String START_RAID_MESSAGE = "The raid has begun!";

    private final Client client;
    private final EventBus eventBus;

    @Getter
    private boolean inRaid;
    @Getter
    private RaidRoom currentRoom;
    @Getter
    private final Map<String, Raider> raiders = new HashMap<>(); // name -> raider

    private int prevRegion;

    public void startUp() {
        clearState();
        eventBus.register(this);
    }

    public void shutDown() {
        eventBus.unregister(this);
        clearState();
    }

    private void clearState() {
        inRaid = false;
        currentRoom = null;
        raiders.clear();
        prevRegion = -1;
    }

    @Subscribe(priority = 5)
    public void onGameTick(GameTick e) {
        if (client.getGameState() != GameState.LOGGED_IN) return;

        int newRegion = getRegion();
        if (newRegion == -1) return;

        if (prevRegion != newRegion) {
            regionChanged(newRegion);
        }
        prevRegion = newRegion;

        if (!inRaid) {
            log.debug("No longer in the raid - clearing raiders!");
            raiders.clear();
            return;
        }

        // If we still haven't loaded any raiders, keep trying. This can happen if the plugin is turned on mid-raid,
        // after the script can run. Or if the script runs but the relevant players aren't actually in the raid
        // yet for the client to retrieve.
        if (raiders.isEmpty()) {
            tryLoadRaiders(true);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc() == null) return;

        int npcId = event.getNpc().getId();
        RaidRoom prevRoom = currentRoom;
        RaidRoom newRoom = RaidRoom.forNpcId(npcId);
        if (newRoom != null && newRoom != prevRoom) {
            log.debug("New room: {}, prev room: {}", newRoom, prevRoom);
            currentRoom = newRoom;
            eventBus.post(RaidRoomChanged.builder().newRaidRoom(newRoom).prevRaidRoom(prevRoom).build());
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (inRaid && event.getGameState() == GameState.LOGGED_IN) {
            log.debug("Reloading raiders since LOGGED_IN game state change");
            tryLoadRaiders(false);
        }
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned event) {
        Player player = event.getPlayer();

        if (player == null) return;

        if (inRaid && raiders.containsKey(player.getName())) {
            log.debug("Updating raider " + player.getName() + " since the spawned while in raid");
            raiders.get(player.getName()).setPlayer(player);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION) return;

        // When the raid is started we can load the raiders
        String message = Text.removeTags(event.getMessage());
        if (START_RAID_MESSAGE.equals(message)) {
            tryLoadRaiders(true);

            boolean newInRaid = true;
            if (newInRaid != inRaid) {
                log.debug("In Raid changed: {}", newInRaid);
                eventBus.post(new InRaidChanged(newInRaid));
            }
            inRaid = newInRaid;
        }
    }

    public boolean isSoloRaid() {
        return raiders.size() == 1;
    }

    public boolean isRaider(Actor actor) {
        return raiders.containsKey(actor.getName());
    }

    private int getRegion() {
        LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
        if (localPoint == null) {
            return -1;
        } else {
            return WorldPoint.fromLocalInstance(client, localPoint).getRegionID();
        }
    }

    private void regionChanged(int newRegion) {
        if (!COX_REGION_IDS.contains(newRegion)) {
            boolean newInRaid = false;
            if (newInRaid != inRaid) {
                log.debug("In Raid changed: {}", newInRaid);
                eventBus.post(new InRaidChanged(newInRaid));
            }
            inRaid = newInRaid;
        }
    }

    private void tryLoadRaiders(boolean newRaid) {
        log.debug("Setting raiders");
        raiders.clear();

        for (Player player : client.getPlayers()) {
            if (player != null &&
                    player.getName() != null &&
                    !raiders.containsKey(player.getName())) {
                raiders.put(player.getName(), new Raider(player));
            }
        }

        log.debug("Loaded raiderNames: {}", raiders.keySet());
        log.debug("Loaded raiders: {}", raiders.keySet());

        if (raiders.isEmpty()) {
            log.debug("Not enough raiders loaded. Will try again later...");
            raiders.clear();
            return;
        }

        if (newRaid) {
            log.debug("New raid");
            eventBus.post(new RaidEntered(ImmutableList.copyOf(raiders.keySet())));
        }
    }
}

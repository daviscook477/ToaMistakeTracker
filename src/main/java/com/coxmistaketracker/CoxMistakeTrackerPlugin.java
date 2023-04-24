package com.coxmistaketracker;

import com.coxmistaketracker.detector.MistakeDetectorManager;
import com.coxmistaketracker.detector.tracker.VengeanceTracker;
import com.coxmistaketracker.events.InRaidChanged;
import com.coxmistaketracker.events.RaidEntered;
import com.coxmistaketracker.panel.CoxMistakeTrackerPanel;
import com.google.inject.Provides;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Cox Mistake Tracker"
)
public class CoxMistakeTrackerPlugin extends Plugin {

    /*
     * An amount of ticks greater than the player's death animation but less
     * than the amount of ticks it will take them to get back from the respawn point
     * to a part of chambers that can have mistakes.
     */
    public static final int DEAD_TICKS = 10;
    public static final int CYCLES_PER_GAME_TICK = Constants.GAME_TICK_LENGTH / Constants.CLIENT_TICK_LENGTH;

    static final String CONFIG_GROUP = "coxMistakeTracker";

    private static final int OVERHEAD_TEXT_TICK_TIMEOUT = 5;
    private static final int CYCLES_FOR_OVERHEAD_TEXT = OVERHEAD_TEXT_TICK_TIMEOUT * CYCLES_PER_GAME_TICK;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private CoxMistakeTrackerConfig config;

    @Inject
    private EventBus eventBus;

    @Inject
    private MistakeDetectorManager mistakeDetectorManager;

    @Inject
    private RaidState raidState;

    @Inject
    private VengeanceTracker vengeanceTracker;

    @Inject
    private OverlayManager overlayManager;

    // UI fields
    @Inject
    private ClientToolbar clientToolbar;
    private final BufferedImage icon = ImageUtil.loadImageResource(CoxMistakeTrackerPlugin.class, "panel-icon.png");
    private CoxMistakeTrackerPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        // Can't @Inject because we null it out in shutdown()
        panel = injector.getInstance(CoxMistakeTrackerPanel.class);

        // Add UI
        panel.loadHeaderIcon(icon);
        navButton = NavigationButton.builder()
                .tooltip("Cox Mistake Tracker")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);

        // Start raid state detection
        clientThread.invoke(() -> {
            raidState.startUp();
        });

        // Reload the panel with all loaded mistakes
        panel.reload();
    }

    @Override
    protected void shutDown() throws Exception {
        // Clear all state
        raidState.shutDown();
        mistakeDetectorManager.shutdown();

        // Remove UI
        clientToolbar.removeNavigation(navButton);
        panel = null;
    }

    // This should run *after* all detectors have handled the GameTick.
    @Subscribe(priority = -1)
    public void onGameTick(GameTick event) {
        if (!raidState.isInRaid()) return;

        // Mark raiders as no longer dead after DEAD_TICKS has passed since their death
        reviveRaiders();

        // Try detecting all possible mistakes for this GameTick
        detectAll();

        // Invoke post-processing method for detectors to get ready for the next GameTick
        afterDetectAll();
    }

    private void reviveRaiders() {
        int currentTick = client.getTickCount();
        for (Raider raider : raidState.getRaiders().values()) {
            if (raider.isDead()) {
                int deadTicks = currentTick - raider.getDeadTick();
                if (deadTicks >= DEAD_TICKS) {
                    raider.setDead(false);
                }
            }
        }
    }

    private void detectAll() {
        for (Raider raider : raidState.getRaiders().values()) {
            if (raider != null) {
                // detect individual mistakes
                detectMistakes(raider);
            }
            log.debug("player " + raider.getName() + " position " + raider.getPlayer().getWorldLocation());
        }

        // detect team mistakes
        detectTeamMistakes();
    }

    private void detectMistakes(@NonNull Raider raider) {
        List<CoxMistake> mistakes = mistakeDetectorManager.detectMistakes(raider);
        if (!mistakes.isEmpty()) {
            log.debug(client.getTickCount() + " Found mistakes for " + raider.getName() + " - " + mistakes);

            for (CoxMistake mistake : mistakes) {
                // Handle special logic for deaths
                if (mistake == CoxMistake.DEATH) {
                    raider.setDead(true);
                    raider.setDeadTick(client.getTickCount());
                }

                addChatMessageForMistake(raider, mistake);
                addMistakeToOverlayPanel(raider, mistake);
            }
        }

        afterDetect(raider);
    }

    private void detectTeamMistakes() {
        List<CoxMistake> mistakes = mistakeDetectorManager.detectTeamMistakes();
        if (!mistakes.isEmpty()) {
            log.debug(client.getTickCount() + " Found mistakes for the team - " + mistakes);

            for (CoxMistake mistake : mistakes) {
                addChatMessageForMistake(mistake);
                addMistakeToOverlayPanel(mistake);
            }
        }
    }

    private void afterDetect(Raider raider) {
        raider.setPreviousWorldLocationForOverlay(raider.getPreviousWorldLocation());
        raider.setPreviousWorldLocation(raider.getCurrentWorldLocation());
    }

    private void afterDetectAll() {
        mistakeDetectorManager.afterDetect();
    }

    private void addChatMessageForMistake(Raider raider, CoxMistake mistake) {
        int mistakeCount = panel.getCurrentTotalMistakeCountForPlayer(raider.getName());
        String msg = CoxMistake.getChatMessageForMistakeCount(config, mistake, mistakeCount);

        // Truncate message length to prevent overly-spammy messages taking up too much screen space
        if (msg.length() > CoxMistake.MAX_MESSAGE_LENGTH) {
            msg = msg.substring(0, CoxMistake.MAX_MESSAGE_LENGTH);
        }

        if (msg.isEmpty()) return;

        // Add to overhead text if config is enabled
        final Player player = raider.getPlayer();
        if (config.showMistakesOnOverheadText()) {
            String overheadText = msg;
            if (vengeanceTracker.didPopVengeance(raider)) {
                overheadText = VengeanceTracker.VENGEANCE_TEXT + " " + overheadText;
            }
            player.setOverheadText(overheadText);
            player.setOverheadCycle(CYCLES_FOR_OVERHEAD_TEXT);
        }

        // Add to chat box if config is enabled
        if (config.showMistakesInChat()) {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, player.getName(), msg, null);
        }
    }

    private void addChatMessageForMistake(CoxMistake mistake) {
        int mistakeCount = panel.getCurrentTotalMistakeCountForTeam();
        String msg = CoxMistake.getChatMessageForMistakeCount(config, mistake, mistakeCount);

        // Truncate message length to prevent overly-spammy messages taking up too much screen space
        if (msg.length() > CoxMistake.MAX_MESSAGE_LENGTH) {
            msg = msg.substring(0, CoxMistake.MAX_MESSAGE_LENGTH);
        }

        if (msg.isEmpty()) return;

        NPC npcForMessage = null;
        List<NPC> npcs = client.getNpcs();
        for (NPC npc : npcs) {
            if (mistake.getNpcIds().contains(npc.getId())) {
                npcForMessage = npc;

                // Add to overhead text if config is enabled
                if (config.showMistakesOnOverheadText()) {
                    npcForMessage.setOverheadText(msg);
                    npcForMessage.setOverheadCycle(CYCLES_FOR_OVERHEAD_TEXT);
                }
            }
        }



        // Add to chat box if config is enabled
        if (config.showMistakesInChat() && npcForMessage != null) {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, npcForMessage.getName(), msg, null);
        }
    }

    private void addMistakeToOverlayPanel(Raider raider, CoxMistake mistake) {
        // Certain mistakes have their own detection and chat messages, but should be grouped together as one in the
        // tracker panel and written state.
        CoxMistake groupedMistake = CoxMistake.toGroupedMistake(mistake);
        SwingUtilities.invokeLater(() -> panel.addMistakeForPlayer(raider.getName(), groupedMistake));
    }

    private void addMistakeToOverlayPanel(CoxMistake mistake) {
        CoxMistake groupedMistakes = CoxMistake.toGroupedMistake(mistake);
        SwingUtilities.invokeLater(() -> panel.addMistakeForTeam(groupedMistakes));
    }

    @Subscribe
    public void onInRaidChanged(InRaidChanged e) {
        if (e.isInRaid()) {
            log.debug("Starting detectors");
            mistakeDetectorManager.startup();
        } else {
            log.debug("Shutting down detectors");
            mistakeDetectorManager.shutdown();
        }
    }

    @Subscribe
    public void onRaidEntered(RaidEntered event) {
        panel.newRaid(event.getRaiderNames());
    }

    @Provides
    CoxMistakeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CoxMistakeTrackerConfig.class);
    }
}

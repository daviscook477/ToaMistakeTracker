package com.coxmistaketracker.detector;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.RaidState;
import com.coxmistaketracker.Raider;
import com.coxmistaketracker.detector.death.DeathDetector;
import com.coxmistaketracker.detector.olm.*;
import com.coxmistaketracker.detector.rooms.*;
import com.coxmistaketracker.detector.tracker.BaseRaidTracker;
import com.coxmistaketracker.detector.tracker.VengeanceTracker;
import com.coxmistaketracker.events.RaidRoomChanged;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manager for all the {@link BaseMistakeDetector}. It keeps all the detectors in memory in order to manage events.
 * <p>
 * All detectors initialized in the manager are responsible for determining when to start detecting mistakes.
 * The manager may call the startup() or shutdown() method on a detector at any time.
 * <p>
 * When the manager is on (started = true), then all other detectors are subscribed to the EventBus and
 * listening for events on when to turn themselves on/off. This will only be true while the player is in Cox.
 */
@Slf4j
@Singleton
public class MistakeDetectorManager {

    private final Client client;
    private final EventBus eventBus;
    private final RaidState raidstate;
    private final VengeanceTracker vengeanceTracker;

    @Getter
    private final List<BaseRaidTracker> raidTrackers;

    @Getter
    private final List<BaseMistakeDetector> mistakeDetectors;

    @Getter
    @VisibleForTesting
    private boolean started;

    @Inject
    public MistakeDetectorManager(Client client,
                                  EventBus eventBus,
                                  RaidState raidState,
                                  VengeanceTracker vengeanceTracker,
                                  ShamansDetector shamansDetector,
                                  TektonDetector tektonDetector,
                                  IceDemonDetector iceDemonDetector,
                                  MuttadilesDetector muttadilesDetector,
                                  VanguardsDetector vanguardsDetector,
                                  VasaDetector vasaDetector,
                                  GuardiansDetector guardiansDetector,
                                  VespulaDetector vespulaDetector,
                                  AcidDetector acidDetector,
                                  CenterDetector centerDetector,
                                  HealDetector healDetector,
                                  FallingCrystalDetector fallingCrystalDetector,
                                  PrayerOrbDetector prayerOrbDetector,
                                  BombDetector bombDetector,
                                  SpecialDetector specialDetector,
                                  DeathDetector deathDetector) {
        this.raidTrackers = Arrays.asList(vengeanceTracker);

        // Order matters, since it's last write wins for which mistake gets put on overhead text. Death should be last.
        this.mistakeDetectors = new ArrayList<>(Arrays.asList(
                shamansDetector,
                tektonDetector,
                iceDemonDetector,
                muttadilesDetector,
                vanguardsDetector,
                vasaDetector,
                guardiansDetector,
                vespulaDetector,
                acidDetector,
                centerDetector,
                healDetector,
                fallingCrystalDetector,
                prayerOrbDetector,
                bombDetector,
                specialDetector,
                deathDetector
        ));

        this.client = client;
        this.eventBus = eventBus;
        this.raidstate = raidState;
        this.vengeanceTracker = vengeanceTracker;
        this.started = false;
    }

    public void startup() {
        started = true;
        eventBus.register(this);

        // Startup all raid trackers
        raidTrackers.forEach(BaseRaidTracker::startup);

        // Startup any detectors that should be active in *all* rooms
        mistakeDetectors.stream().filter(d -> d.getRaidRoom() == null).forEach(BaseMistakeDetector::startup);

        // Startup any detectors that should be active in the current room
        mistakeDetectors.stream().filter(d -> d.getRaidRoom() == raidstate.getCurrentRoom()).forEach(BaseMistakeDetector::startup);
    }

    public void shutdown() {
        mistakeDetectors.forEach(BaseMistakeDetector::shutdown);
        // Don't clear mistakeDetectors or else we can't get them back.

        raidTrackers.forEach(BaseRaidTracker::shutdown);

        eventBus.unregister(this);
        started = false;
    }

    public List<CoxMistake> detectMistakes(@NonNull Raider raider) {
        if (!started) return Collections.emptyList();

        List<CoxMistake> mistakes = new ArrayList<>();
        for (BaseMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes() && !raider.isDead()) {
                mistakes.addAll(mistakeDetector.detectMistakes(raider));
            }
        }

        return mistakes;
    }

    public List<CoxMistake> detectTeamMistakes() {
        if (!started) return Collections.emptyList();

        List<CoxMistake> mistakes = new ArrayList<>();
        for (BaseMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakes.addAll(mistakeDetector.detectTeamMistakes());
            }
        }

        return mistakes;
    }

    public void afterDetect() {
        if (!started) return;

        raidTrackers.forEach(BaseRaidTracker::afterDetect);

        for (BaseMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakeDetector.afterDetect();
            }
        }
    }

    @Subscribe
    public void onRaidRoomChanged(RaidRoomChanged event) {
        if (event.getNewRaidRoom() == event.getPrevRaidRoom()) {
            log.debug("Raid room did not change!");
            return;
        }
        // Detectors that run in *all* rooms do not need to handle these events
        mistakeDetectors.stream().filter(d -> d.getRaidRoom() != null).forEach(detector -> {
            if (detector.getRaidRoom() == event.getNewRaidRoom()) {
                detector.startup();
            } else if (detector.getRaidRoom() == event.getPrevRaidRoom()) {
                detector.shutdown();
            }
        });
    }
}

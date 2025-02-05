package com.coxmistaketracker.mistakestate;

import com.coxmistaketracker.CoxMistake;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps track of mistakes for players
 */
class MistakeManager {

    private final Map<String, PlayerTrackingInfo> trackingInfo;
    private TeamTrackingInfo teamTrackingInfo;
    private int trackedRaids;

    MistakeManager() {
        trackingInfo = new HashMap<>();
        teamTrackingInfo = new TeamTrackingInfo();
        trackedRaids = 0;
    }

    public void clearAllMistakes() {
        trackingInfo.clear();
        teamTrackingInfo = new TeamTrackingInfo();
        trackedRaids = 0;
    }

    public void addMistakeForPlayer(String playerName, CoxMistake mistake) {
        PlayerTrackingInfo playerInfo = trackingInfo.computeIfAbsent(playerName,
                k -> new PlayerTrackingInfo(playerName));
        playerInfo.incrementMistake(mistake);
    }

    public void addMistakeForTeam(CoxMistake mistake) {
        teamTrackingInfo.incrementMistake(mistake);
    }

    public void newRaid(Set<String> playerNames) {
        // TODO: Small bug where if plugin is installed mid-raid (or mistakes reset), then player raids gets 1 but
        // total tracked raids is still 0
        trackedRaids++;

        for (String playerName : playerNames) {
            PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
            if (playerInfo != null) {
                playerInfo.incrementRaidCount();
            } else {
                trackingInfo.put(playerName, new PlayerTrackingInfo(playerName));
            }
        }
    }

    public void removeAllMistakesForPlayer(String playerName) {
        trackingInfo.remove(playerName);
    }

    public void removeAllMistakesForTeam() {
        teamTrackingInfo = new TeamTrackingInfo();
    }

    public Set<String> getPlayersWithMistakes() {
        return trackingInfo.values().stream()
                .filter(PlayerTrackingInfo::hasMistakes)
                .map(PlayerTrackingInfo::getPlayerName)
                .collect(Collectors.toSet());
    }

    public int getMistakeCountForPlayer(String playerName, CoxMistake mistake) {
        PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
        if (playerInfo != null) {
            Integer count = playerInfo.getMistakes().get(mistake);
            if (count != null) {
                return count;
            }
        }

        return 0;
    }

    public int getMistakeCountForTeam(CoxMistake mistake) {
        Integer count = teamTrackingInfo.getMistakes().get(mistake);
        if (count != null) {
            return count;
        }

        return 0;
    }

    public int getTotalMistakeCountForPlayer(String playerName) {
        int totalMistakes = 0;
        PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
        if (playerInfo != null) {
            for (int mistakes : playerInfo.getMistakes().values()) {
                totalMistakes += mistakes;
            }
        }
        return totalMistakes;
    }

    public int getTotalMistakeCountForTeam() {
        int totalMistakes = 0;
        for (int mistakes : teamTrackingInfo.getMistakes().values()) {
            totalMistakes += mistakes;
        }
        return totalMistakes;
    }

    public int getTotalMistakeCountForAllPlayers() {
        // TODO: Fix bug where room death and raid death count as 2 distinct mistakes, but they're the same.
        int totalMistakes = 0;
        for (PlayerTrackingInfo playerInfo : trackingInfo.values()) {
            for (int mistakes : playerInfo.getMistakes().values()) {
                totalMistakes += mistakes;
            }
        }

        return totalMistakes;
    }

    public int getRaidCountForPlayer(String playerName) {
        PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
        if (playerInfo != null) {
            return playerInfo.getRaidCount();
        }

        return 0;
    }

    public int getTrackedRaids() {
        return trackedRaids;
    }
}

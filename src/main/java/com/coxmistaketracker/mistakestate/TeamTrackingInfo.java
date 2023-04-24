package com.coxmistaketracker.mistakestate;

import com.coxmistaketracker.CoxMistake;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulating class for relevant tracking information for the team, including mistakes.
 */
public class TeamTrackingInfo {

    @NonNull
    @Getter
    private final Map<CoxMistake, Integer> mistakes;

    public TeamTrackingInfo() {
        this.mistakes = new HashMap<>();
    }

    public void incrementMistake(CoxMistake mistake) {
        mistakes.compute(mistake, TeamTrackingInfo::increment);
    }

    public boolean hasMistakes() {
        return !mistakes.isEmpty();
    }

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }
}

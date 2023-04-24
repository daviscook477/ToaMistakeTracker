package com.coxmistaketracker;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;

import java.util.Set;

public enum RaidRoom {
    OLM(ImmutableSet.of(7550, 7551, 7552, 7553, 7554, 7555)),
    ICE_DEMON(ImmutableSet.of(7584, 7585)),
    CRABS(ImmutableSet.of(7576, 7577, 7578, 7579)),
    SHAMANS(ImmutableSet.of(7573, 7574)),
    MUTTADILES(ImmutableSet.of(7561, 7562, 7563)),
    MYSTICS(ImmutableSet.of(7604, 7605, 7606)),
    TEKTON(ImmutableSet.of(7540, 7541, 7542, 7543, 7544, 7545)),
    VANGUARDS(ImmutableSet.of(7525, 7526, 7527, 7528, 7529)),
    VASA(ImmutableSet.of(7565, 7566, 7567)),
    VESPULA(ImmutableSet.of(7530, 7531, 7532)),
    GUARDIANS(ImmutableSet.of(7569, 7570, 7571, 7572)),
    TIGHTROPE(ImmutableSet.of(7559, 7560));

    RaidRoom(Set<Integer> npcIds) {
        this.npcIds = npcIds;
    }

    @Getter
    private final Set<Integer> npcIds;

    public static RaidRoom forNpcId(int npcId) {
        for (RaidRoom r : RaidRoom.values()) {
            if (r.getNpcIds().contains(npcId)) {
                return r;
            }
        }

        return null;
    }
}

package com.coxmistaketracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CoxMistakeTrackerPlugin.CONFIG_GROUP)
public interface CoxMistakeTrackerConfig extends Config {

    @ConfigItem(
            keyName = "showMistakesInChat",
            name = "Show Mistakes In Chat",
            description = "When a player makes a mistake in CoX, whether or not to log the mistake message to your " +
                    "public chat. Max message length of " + CoxMistake.MAX_MESSAGE_LENGTH + " to prevent spamming chat.",
            position = 0
    )
    default boolean showMistakesInChat() {
        return true;
    }

    @ConfigItem(
            keyName = "showMistakesOnOverheadText",
            name = "Show Mistakes On Overhead Text",
            description = "When a player makes a mistake in Cox, whether or not to show the mistake message above " +
                    "their head as overhead text.",
            position = 1
    )
    default boolean showMistakesOnOverheadText() {
        return true;
    }

    @ConfigSection(
            name = "Death Messages",
            description = "Settings for the messages shown on Death mistakes (separate multiple messages by \"|\").",
            position = 2,
            closedByDefault = true
    )
    String deathMistakeSettings = "deathMistakeSettings";

    @ConfigItem(
            keyName = "deathMessage",
            name = "Death",
            description = "Message to show on death.",
            section = deathMistakeSettings,
            position = 0
    )
    default String deathMessage() {
        return CoxMistake.defaultDeathMessage();
    }

    @ConfigSection(
            name = "Room Messages",
            description = "Settings for the messages shown on room mistakes (separate multiple messages by \"|\").",
            position = 3,
            closedByDefault = true
    )
    String roomMistakeSettings = "roomMistakeSettings";

    @ConfigItem(
            keyName = "shamansGooMessage",
            name = "Shamans Goo",
            description = "Message to show when hit by the shamans' poison blob.",
            section = roomMistakeSettings,
            position = 0
    )
    default String shamansGooMessage() {
        return CoxMistake.SHAMANS_GOO.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "shamansSpawnMessage",
            name = "Shamans Spawn Explosion",
            description = "Message to show when hit by the shamans' spawn explosion.",
            section = roomMistakeSettings,
            position = 1
    )
    default String shamansSpawnMessage() {
        return CoxMistake.SHAMANS_SPAWN_EXPLOSION.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "tektonMeleeMesssage",
            name = "Tekton Melee",
            description = "Message to show when hit by tekton's melee attack w/o vengeance up.",
            section = roomMistakeSettings,
            position = 2
    )
    default String tektonMeleeMesssage() {
        return CoxMistake.TEKTON_MELEE.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "te",
            name = "Tekton Sparks",
            description = "Message to show when hit by tekton's sparks.",
            section = roomMistakeSettings,
            position = 3
    )
    default String tektonSparksMessage() {
        return CoxMistake.TEKTON_SPARKS.getDefaultMessage();
    }


    @ConfigItem(
            keyName = "iceDemonRangedMessage",
            name = "Ice Demon Ranged Hit",
            description = "Message to show when hit by the ice demon's ranged attack w/o vengeance up.",
            section = roomMistakeSettings,
            position = 4
    )
    default String iceDemonRangedMessage() {
        return CoxMistake.ICE_DEMON_RANGED.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "iceDemonMageMessage",
            name = "Ice Demon Mage Hit",
            description = "Message to show when hit by the ice demon's mage attack w/o vengeance up.",
            section = roomMistakeSettings,
            position = 5
    )
    default String iceDemonMageMessage() {
        return CoxMistake.ICE_DEMON_MAGE.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "vasaBoulderMessage",
            name = "Vasa Boulder Hit",
            description = "Message to show when hit by vasa's boulder attack w/o vengeance up.",
            section = roomMistakeSettings,
            position = 6
    )
    default String vasaBoulderMessage() {
        return CoxMistake.VASA_BOULDER.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "guardiansBoulderMessage",
            name = "Guardians Boulder Hit",
            description = "Message to show when hit by the guardians falling boulder projectile.",
            section = roomMistakeSettings,
            position = 7
    )
    default String guardiansBoulderMessage() {
        return CoxMistake.GUARDIANS_BOULDER.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "muttadilesHealMessage",
            name = "Muttadiles Heal",
            description = "Message to show when the muttadiles heal at the meat tree.",
            section = roomMistakeSettings,
            position = 8
    )
    default String muttadilesHealMessage() {
        return CoxMistake.SMALL_MUTTADILE_HEAL.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "vanguardsResetMessage",
            name = "Vanguards Reset",
            description = "Message to show when the vanguards reset because their HPs did not stay close enough together.",
            section = roomMistakeSettings,
            position = 9
    )
    default String vanguardsResetMessage() {
        return CoxMistake.VANGUARDS_RESET.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "vespulaLuxGrubHatchedMessage",
            name = "Vespula Lux Grub Hatched",
            description = "Message to show when the one of vespula's lux grubs hatches into a vespine soldier.",
            section = roomMistakeSettings,
            position = 10
    )
    default String vespulaLuxGrubHatchedMessage() {
        return CoxMistake.VESPULA_LUX_GRUB_HATCHED.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "vespulaLuxGrubHitMessage",
            name = "Vespula Lux Grub Hit",
            description = "Message to show when the one of vespula's lux gets hit/stung by vespula.",
            section = roomMistakeSettings,
            position = 11
    )
    default String vespulaLuxGrubHitMessage() {
        return CoxMistake.VESPULA_LUX_GRUB_HIT.getDefaultMessage();
    }

    @ConfigSection(
            name = "Olm Messages",
            description = "Settings for the messages shown on olm mistakes (separate multiple messages by \"|\").",
            position = 4,
            closedByDefault = true
    )
    String olmMistakeSettings = "olmMistakeSettings";

    @ConfigItem(
            keyName = "olmCenterMessage",
            name = "Olm Head Centered",
            description = "Message to show when the olm turns its head to the center.",
            section = olmMistakeSettings,
            position = 0
    )
    default String olmCenterMessage() {
        return CoxMistake.OLM_CENTER.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmPrayerMessage",
            name = "Olm Prayer Orb",
            description = "Message to show when hit by one of the olm's prayer orbs.",
            section = olmMistakeSettings,
            position = 1
    )
    default String olmPrayerMessage() {
        return CoxMistake.OLM_PRAYER_ORB.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmSpecialOccursMessage",
            name = "Olm Special Occurs",
            description = "Message to show when the olm performs a skippable special attack.",
            section = olmMistakeSettings,
            position = 1
    )
    default String olmSpecialOccursMessage() {
        return CoxMistake.defaultSpecialOccursMessage();
    }

    @ConfigItem(
            keyName = "olmCrystalsMessage",
            name = "Olm Crystals Damage",
            description = "Message to show when hit by the crystals special.",
            section = olmMistakeSettings,
            position = 2
    )
    default String olmCrystalsMessage() {
        return CoxMistake.OLM_SPECIAL_CRYSTALS_DAMAGE.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmLightningMessage",
            name = "Olm Lightning Damage",
            description = "Message to show when hit by the lightning special.",
            section = olmMistakeSettings,
            position = 3
    )
    default String olmLightningMessage() {
        return CoxMistake.OLM_SPECIAL_LIGHTNING_DAMAGE.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmTeleportsMessage",
            name = "Olm Teleports Damage",
            description = "Message to show when teleported by the teleports special.",
            section = olmMistakeSettings,
            position = 4
    )
    default String olmTeleportsMessage() {
        return CoxMistake.OLM_SPECIAL_TELEPORTS_DAMAGE.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmCrystalBombMessage",
            name = "Olm Crystal Bomb Explosion",
            description = "Message to show when hit by the most damaging part (3x3) of the crystal bomb explosion.",
            section = olmMistakeSettings,
            position = 5
    )
    default String olmCrystalBombMessage() {
        return CoxMistake.OLM_CRYSTAL_BOMB_EXPLOSION.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmFlameWallMessage",
            name = "Olm Flame Wall Damage",
            description = "Message to show when damaged by staying in the flame wall when it pops.",
            section = olmMistakeSettings,
            position = 6
    )
    default String olmFlameWallMessage() {
        return CoxMistake.OLM_FLAME_WALL_DAMAGE.getDefaultMessage();
    }


    @ConfigItem(
            keyName = "olmLeftClawHealMessage",
            name = "Olm Left Claw Healing",
            description = "Message to show when attacking the left claw while the healing special is going on.",
            section = olmMistakeSettings,
            position = 7
    )
    default String olmLeftClawHealMessage() {
        return CoxMistake.OLM_LEFT_CLAW_HEAL.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmClawResetMessage",
            name = "Olm Claw Reset",
            description = "Message to show when resetting one of the olm's claws by not killing them both at the same time in the last hand phase.",
            section = olmMistakeSettings,
            position = 8
    )
    default String olmClawResetMessage() {
        return CoxMistake.OLM_CLAW_RESET.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmFallingCrystalMessage",
            name = "Olm Falling Crystals",
            description = "Message to show when hit by a falling crystal outside of the falling crystal attack..",
            section = olmMistakeSettings,
            position = 9
    )
    default String olmFallingCrystalsMessage() {
        return CoxMistake.OLM_FALLING_CRYSTALS.getDefaultMessage();
    }

    @ConfigItem(
            keyName = "olmFallingCrystalAttackMessage",
            name = "Olm Falling Crystal Attack",
            description = "Message to show when hit by a falling crystal during the falling crystal attack.",
            section = olmMistakeSettings,
            position = 10
    )
    default String olmFallingCrystalAttackMessage() {
        return CoxMistake.OLM_FALLING_CRYSTAL_ATTACK.getDefaultMessage();
    }
}

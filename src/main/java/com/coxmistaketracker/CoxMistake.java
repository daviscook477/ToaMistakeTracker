package com.coxmistaketracker;

import lombok.Getter;
import lombok.NonNull;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum CoxMistake {
    // Deaths
    DEATH("Death", (config) -> "", "", "death.png"), // Chat message handled in deaths below
    DEATH_ICE_DEMON("Ice Demon Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death-ice-demon.png"),
    DEATH_SHAMANS("Shamans Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    DEATH_MUTTADILES("Muttadiles Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death-muttadiles.png"),
    DEATH_MYSTICS("Mystics Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    DEATH_TEKTON("Tekton Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death-tekton.png"),
    DEATH_VANGUARDS("Vanguards Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death-vanguards.png"),
    DEATH_VASA("Vasa Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    DEATH_VESPULA("Vespula Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    DEATH_GUARDIANS("Guardians Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death-guardians.png"),
    DEATH_TIGHTROPE("Tighrope Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    DEATH_OLM("Olm Death", CoxMistakeTrackerConfig::deathMessage, defaultDeathMessage(), "death.png"),
    SHAMANS_GOO("Shamans Goo", CoxMistakeTrackerConfig::shamansGooMessage, "I'm getting gooed!", "death.png"),
    TEKTON_SPARKS("Tekton Sparks", CoxMistakeTrackerConfig::tektonSparksMessage, "I'm feeling sparky!", "death.png"),
    ICE_DEMON_RANGED("Ice Demon Ranged Hit", CoxMistakeTrackerConfig::iceDemonRangedMessage, "I'm eating boulders!", "death.png"),
    ICE_DEMON_MAGE("Ice Demon Mage Hit", CoxMistakeTrackerConfig::iceDemonMageMessage, "I'm a popsicle!", "death.png"),
    VASA_BOULDER("Vasa Boulder Hit", CoxMistakeTrackerConfig::vasaBoulderMessage, "I'm eating boulders!", "death.png"),
    GUARDIANS_BOULDER("Guardians Boulder Hit", CoxMistakeTrackerConfig::guardiansBoulderMessage, "I'm eating boulders!", "death.png"),
    OLM_ACID_STANDSTILL("Olm Acid Standstill", (config -> ""), "", "death.png"),
    OLM_FALLING_CRYSTAL_BETWEEN_PHASES("Olm Falling Crystal Between Phases", CoxMistakeTrackerConfig::olmFallingCrystalBetweenPhasesMessage, defaultFallingCrystalMessage(), "death.png"),
    OLM_FALLING_CRYSTAL_ATTACK("Olm Falling Crystal Attack", CoxMistakeTrackerConfig::olmFallingCrystalAttackMessage, defaultFallingCrystalMessage(), "death.png"),
    OLM_CENTER("Olm Head Centered", CoxMistakeTrackerConfig::olmCenterMessage, "I'm centering!", "death.png"),
    OLM_PRAYER_ORB("Olm Prayer Orb", CoxMistakeTrackerConfig::olmPrayerMessage, "What even was that attack?", "death.png"),
    OLM_SPECIAL_CRYSTAL_OCCURS("Olm Crystals Special Occurs", CoxMistakeTrackerConfig::olmSpecialOccursMessage, defaultSpecialOccursMessage(), "death.png"),
    OLM_SPECIAL_LIGHTNING_OCCURS("Olm Lightning Special Occurs", CoxMistakeTrackerConfig::olmSpecialOccursMessage, defaultSpecialOccursMessage(), "death.png"),
    OLM_SPECIAL_TELEPORTS_OCCURS("Olm Teleports Special Occurs", CoxMistakeTrackerConfig::olmSpecialOccursMessage, defaultSpecialOccursMessage(), "death.png"),
    OLM_SPECIAL_CRYSTALS_DAMAGE("Olm Crystals Special Damage", CoxMistakeTrackerConfig::olmCrystalsMessage, "I'm bursting!", "death.png"),
    OLM_SPECIAL_LIGHTNING_DAMAGE("Olm Lightning Special Damage", CoxMistakeTrackerConfig::olmLightningMessage, "I'm shocked!", "death.png"),
    OLM_SPECIAL_TELEPORTS_DAMAGE("Olm Teleports Special Damage", CoxMistakeTrackerConfig::olmTeleportsMessage, "I'm going on a trip!", "death.png"),
    OLM_CRYSTAL_BOMB_EXPLOSION("Olm Crystal Bomb Explosion", CoxMistakeTrackerConfig::olmCrystalBombMessage, "I'm exploding!", "death.png"),
    OLM_LEFT_CLAW_HEAL("Olm Left Claw Heal", CoxMistakeTrackerConfig::olmLeftClawHealMessage, "I'm healing the claw!", "death.png"),
    OLM_CLAW_RESET("Olm Claw Reset", CoxMistakeTrackerConfig::olmClawResetMessage, "I can do this all day!", "death.png");

    private static final Set<CoxMistake> ROOM_DEATHS = EnumSet.of(DEATH, DEATH_ICE_DEMON, DEATH_SHAMANS, DEATH_MUTTADILES, DEATH_MYSTICS, DEATH_TEKTON, DEATH_VANGUARDS, DEATH_VASA, DEATH_VESPULA, DEATH_GUARDIANS, DEATH_TIGHTROPE, DEATH_OLM);

    private static final String FALLBACK_IMAGE_PATH = "death.png";

    public static final int MAX_MESSAGE_LENGTH = 40;

    @Getter
    @NonNull
    private final String mistakeName;

    @Getter
    @NonNull
    private final String defaultMessage;

    @NonNull
    private final Function<CoxMistakeTrackerConfig, String> chatMessageFunc;

    @Getter
    @NonNull
    private final BufferedImage mistakeImage;

    CoxMistake(@NonNull String mistakeName, @NonNull Function<CoxMistakeTrackerConfig, String> chatMessageFunc, @NonNull String defaultMessage,
               @NonNull String mistakeImagePath) {
        this.mistakeName = mistakeName;
        this.chatMessageFunc = chatMessageFunc;
        this.defaultMessage = defaultMessage;

        final String imagePath;
        if (mistakeImagePath.isEmpty()) {
            imagePath = FALLBACK_IMAGE_PATH;
        } else {
            imagePath = mistakeImagePath;
        }
        this.mistakeImage = ImageUtil.loadImageResource(getClass(), imagePath);
    }

    public String getChatMessage(CoxMistakeTrackerConfig config) {
        return chatMessageFunc.apply(config);
    }

    public static String defaultDeathMessage() {
        return "I'm planking!";
    }

    public static String defaultSpecialOccursMessage() {
        return "I'm feeling special!";
    }

    public static String defaultFallingCrystalMessage() {
        return "Ouch that's sharp!";
    }

    public static boolean isRoomDeath(CoxMistake mistake) {
        return ROOM_DEATHS.contains(mistake);
    }

    /**
     * Get the grouped mistake for the specified detected mistake.
     *
     * @param mistake The detected mistake
     * @return The grouped mistake
     */
    public static CoxMistake toGroupedMistake(CoxMistake mistake) {
        return mistake;
    }

    /**
     * Retrieve the chat message for the given mistake, considering special cases given the config
     * settings about whether to stack question marks and the current mistake count of either
     * this specific mistake in the raid for the current raider, *or* the total current raid mistake
     * count for the current raider.
     *
     * @param config       The configuration object to retrieve chat message from
     * @param mistake      The mistake
     * @param mistakeCount The current mistake count of this mistake *or* the current raid mistake count for the
     *                     raider in this raid
     *
     * @return The mistake chat message to use for the raider
     */
    public static String getChatMessageForMistakeCount(CoxMistakeTrackerConfig config, CoxMistake mistake,
            int mistakeCount) {
        String mistakeMessage = mistake.getChatMessage(config);
        if (mistakeMessage.contains("|")) {
            return getAlternatingChatMessage(mistakeMessage, mistake.getDefaultMessage(), mistakeCount);
        }

        return mistakeMessage;
    }

    private static String getAlternatingChatMessage(String message, String defaultMessage, int mistakeCount) {
        String[] messageChoices = Arrays.stream(message.split("\\|")).filter(msg -> !msg.isEmpty())
                .toArray(String[]::new);
        if (messageChoices.length == 0) {
            return defaultMessage;
        }
        return messageChoices[mistakeCount % messageChoices.length];
    }
}

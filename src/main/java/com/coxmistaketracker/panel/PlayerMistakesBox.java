package com.coxmistaketracker.panel;

import com.coxmistaketracker.CoxMistake;
import com.coxmistaketracker.mistakestate.MistakeStateManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel box for a mistake. This was copied over from ToaMistakeTracker, with minor changes
 */
@Slf4j
public class PlayerMistakesBox extends JPanel {

    private static final int ITEMS_PER_ROW = 5;

    private final MistakeStateManager mistakeStateManager;

    @NonNull
    @Getter
    private final String playerName;
    private int totalMistakes;

    private final JPanel mistakesContainer = new JPanel();
    private final JLabel raidCountLabel;
    private final JLabel mistakeCountLabel;

    protected PlayerMistakesBox(MistakeStateManager mistakeStateManager, @NonNull String playerName) {
        this.mistakeStateManager = mistakeStateManager;
        this.playerName = playerName;

        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(5, 0, 0, 0));

        final JPanel playerMistakesTitle = new JPanel(new BorderLayout(5, 0));
        playerMistakesTitle.setBorder(new EmptyBorder(7, 7, 7, 7));
        playerMistakesTitle.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        final JLabel playerNameLabel = new JLabel(playerName); // e.g. Questing Pet
        playerNameLabel.setFont(FontManager.getRunescapeSmallFont());
        playerNameLabel.setForeground(Color.WHITE);
        playerMistakesTitle.add(playerNameLabel, BorderLayout.WEST);

        mistakeCountLabel = new JLabel(); // e.g. 78 Mistakes
        mistakeCountLabel.setFont(FontManager.getRunescapeSmallFont());
        mistakeCountLabel.setForeground(Color.LIGHT_GRAY);
        mistakeCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        playerMistakesTitle.add(mistakeCountLabel, BorderLayout.CENTER);

        raidCountLabel = new JLabel(); // e.g. (53 Raids)
        raidCountLabel.setFont(FontManager.getRunescapeSmallFont());
        raidCountLabel.setForeground(Color.GRAY);
        playerMistakesTitle.add(raidCountLabel, BorderLayout.EAST);

        add(playerMistakesTitle, BorderLayout.NORTH);
        add(mistakesContainer, BorderLayout.CENTER);

        // Create popup menu for resetting the player's mistakes
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        setComponentPopupMenu(popupMenu);
    }

    void rebuildAllMistakes(boolean isRaidDeaths) {
        buildMistakes(isRaidDeaths);

        final String mistakeString = totalMistakes == 1 ? "Mistake" : "Mistakes";
        mistakeCountLabel.setText(String.format("%s %s", totalMistakes, mistakeString));

        final int totalRaids = playerName.equals("The Team") ? mistakeStateManager.getRaidCountForTeam() : mistakeStateManager.getRaidCountForPlayer(playerName);
        if (totalRaids > 0) {
            final String raidString = totalRaids == 1 ? "Raid" : "Raids";
            raidCountLabel.setText(String.format("(%s %s)", totalRaids, raidString));
        }

        validate();
        repaint();
    }

    private void buildMistakes(boolean isRaidDeaths) {
        totalMistakes = 0;
        setVisible(true);

        List<CoxMistakeCount> mistakeCountsForPlayer = new ArrayList<>();
        for (CoxMistake mistake : CoxMistake.values()) {
            if (isRaidDeaths && CoxMistake.isRoomDeath(mistake)) {
                continue;
            } else if (!isRaidDeaths && mistake == CoxMistake.DEATH) {
                continue;
            }

            int mistakeCount = playerName.equals("The Team") ? mistakeStateManager.getMistakeCountForTeam(mistake) : mistakeStateManager.getMistakeCountForPlayer(playerName, mistake);
            if (mistakeCount > 0) {
                totalMistakes += mistakeCount;
                mistakeCountsForPlayer.add(new CoxMistakeCount(mistake, mistakeCount));
            }
        }

        int numRows = calculateNumRows(mistakeCountsForPlayer.size());

        mistakesContainer.removeAll();
        mistakesContainer.setLayout(new GridLayout(numRows, ITEMS_PER_ROW, 1, 1));

        for (int i = 0; i < numRows * ITEMS_PER_ROW; i++) {
            // Create the panel for this mistake
            final JPanel mistakeContainer = new JPanel(new BorderLayout(0, 0));
            mistakeContainer.setMaximumSize(new Dimension(36, 32));
            mistakeContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            // If we still have mistakes to show for this row
            if (i < mistakeCountsForPlayer.size()) {
                CoxMistakeCount mistakeCount = mistakeCountsForPlayer.get(i);

                // Create the image label
                final JLabel imageLabel = new JLabel();
                imageLabel.setLayout(new BorderLayout(0, 0));
                imageLabel.setToolTipText(mistakeCount.getTooltipText());
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                BufferedImage mistakeImage = mistakeCount.getMistake().getMistakeImage();
                imageLabel.setIcon(new ImageIcon(mistakeImage));

                // Create the quantity label
                final JLabel quantityLabel = new JLabel();
                quantityLabel.setText(String.valueOf(mistakeCount.getCount()));
                quantityLabel.setFont(FontManager.getRunescapeSmallFont());
                quantityLabel.setForeground(Color.YELLOW);
                quantityLabel.setVerticalAlignment(SwingConstants.BOTTOM);
                quantityLabel.setHorizontalAlignment(SwingConstants.LEFT);

                // Add them to the panel
                imageLabel.add(quantityLabel, BorderLayout.NORTH); // Add the quantity to the image so it's on top of it
                mistakeContainer.add(imageLabel);

            }
            mistakesContainer.add(mistakeContainer);
        }

        mistakesContainer.repaint();
    }

    private static int calculateNumRows(int numItems) {
        return (int) Math.ceil((double) numItems / ITEMS_PER_ROW);
    }

    @Value
    private static class CoxMistakeCount {

        CoxMistake mistake;
        int count;
        String tooltipText;

        CoxMistakeCount(CoxMistake mistake, int count) {
            this.mistake = mistake;
            this.count = count;
            this.tooltipText = String.format("%s x %s", mistake.getMistakeName(), count);
        }
    }
}

package mainPackage;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class GameGUI extends JFrame {

    // Shared State
    private final BattleEngine engine = new BattleEngine();
    private final DefaultListModel<GameCharacter> rosterModel = new DefaultListModel<>();
    private final JTextArea battleLog = new JTextArea();

    // Manager Screen
    private final JTextField nameField = new JTextField(12);
    private final JComboBox<String> classBox =
            new JComboBox<>(new String[]{"Warrior", "Mage", "Archer", "Assassin"});
    private final JList<GameCharacter> rosterList = new JList<>(rosterModel);
    private final JLabel managerStatus = new JLabel(" ");

    // Battle Screen
    private final JPanel arenaPanel = new JPanel(new GridLayout(1, 0, 10, 10));
    private final JLabel turnLabel = new JLabel(" ", SwingConstants.CENTER);
    private GameCharacter selectedTarget = null;
    
    // Custom Action Cards (replacing standard JButtons)
    private final JPanel basicCard    = new JPanel();
    private final JPanel skillCard    = new JPanel();
    private final JPanel ultimateCard = new JPanel();
    private final JPanel defendCard   = new JPanel();
    
    private final JButton endTurnBtn  = new JButton("End Turn");

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public GameGUI() {
        super("Turn-Based Battle Arena");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        buildManagerScreen();
        buildBattleScreen();
        add(root);
        cards.show(root, "MANAGER");
    }

    //  SCREEN 1: CHARACTER MANAGER
    private void buildManagerScreen() {
        JPanel screen = new JPanel(new BorderLayout(10, 10));
        screen.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Character Manager", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        screen.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Create Character"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Name:"), g);
        g.gridx = 1;               form.add(nameField, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Class:"), g);
        g.gridx = 1;               form.add(classBox, g);

        JButton createBtn = new JButton("Create");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton startBtn  = new JButton("Start Battle");

        g.gridx = 0; g.gridy = 2; form.add(createBtn, g);
        g.gridx = 1;               form.add(deleteBtn, g);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        form.add(startBtn, g);

        screen.add(form, BorderLayout.WEST);

        // --- roster list ---
        rosterList.setCellRenderer(new RosterRenderer());
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Roster"));
        listPanel.add(new JScrollPane(rosterList), BorderLayout.CENTER);
        screen.add(listPanel, BorderLayout.CENTER);

        managerStatus.setFont(managerStatus.getFont().deriveFont(Font.ITALIC));
        screen.add(managerStatus, BorderLayout.SOUTH);

        // --- listeners ---
        createBtn.addActionListener(e -> createCharacter());
        deleteBtn.addActionListener(e -> deleteCharacter());
        startBtn.addActionListener(e -> startBattle());

        root.add(screen, "MANAGER");
    }

    private void createCharacter() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { managerStatus.setText("Enter a name."); return; }
        for (GameCharacter c : engine.getRoster())
            if (c.getName().equalsIgnoreCase(name)) {
                managerStatus.setText("Name already taken."); return;
            }

        GameCharacter c = switch ((String) classBox.getSelectedItem()) {
            case "Warrior"  -> new Warrior(name);
            case "Mage"     -> new Mage(name);
            case "Archer"   -> new Archer(name);
            case "Assassin" -> new Assassin(name);
            default         -> null;
        };
        if (c == null) return;

        engine.addCharacter(c);
        rosterModel.addElement(c);
        nameField.setText("");
        managerStatus.setText("Created " + c.getName() + " (" + classBox.getSelectedItem() + ").");
    }

    private void deleteCharacter() {
        GameCharacter c = rosterList.getSelectedValue();
        if (c == null) { managerStatus.setText("Select a character to delete."); return; }
        engine.removeCharacter(c.getName());
        rosterModel.removeElement(c);
        managerStatus.setText("Deleted " + c.getName() + ".");
    }

    private void startBattle() {
        engine.reset(); 
        if (!engine.canStartBattle()) {
            managerStatus.setText("Need at least 2 characters to battle.");
            return;
        }
        
        battleLog.setText("");
        refreshArena();
        cards.show(root, "BATTLE");
        nextTurn();
    }

    //  SCREEN 2: BATTLE
    private void buildBattleScreen() {
        JPanel screen = new JPanel(new BorderLayout(10, 10));
        screen.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- top: turn indicator + back button ---
        JPanel top = new JPanel(new BorderLayout());
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 18f));
        top.add(turnLabel, BorderLayout.CENTER);
        JButton backBtn = new JButton("Back to Manager");
        backBtn.addActionListener(e -> cards.show(root, "MANAGER"));
        top.add(backBtn, BorderLayout.EAST);
        screen.add(top, BorderLayout.NORTH);

        // --- center: combatant cards + log ---
        arenaPanel.setBorder(BorderFactory.createTitledBorder("Combatants"));
        JScrollPane arenaScroll = new JScrollPane(arenaPanel);
        arenaScroll.setPreferredSize(new Dimension(0, 260));

        battleLog.setEditable(false);
        battleLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Battle Log"));

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(arenaScroll, BorderLayout.NORTH);
        center.add(logScroll, BorderLayout.CENTER);
        screen.add(center, BorderLayout.CENTER);

        // --- bottom: action bar ---
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        actions.setBorder(BorderFactory.createTitledBorder("Actions"));

        setupActionCard(basicCard, "Basic Attack", Abilities.BASIC_ATTACK);
        setupActionCard(skillCard, "Skill", Abilities.SKILL);
        setupActionCard(ultimateCard, "Ultimate", Abilities.ULTIMATE);
        setupActionCard(defendCard, "Defend", Abilities.DEFEND);

        actions.add(basicCard);
        actions.add(skillCard);
        actions.add(ultimateCard);
        actions.add(defendCard);
        actions.add(endTurnBtn);
        screen.add(actions, BorderLayout.SOUTH);

        // Listeners
        endTurnBtn.addActionListener(e -> endTurn());
        
        root.add(screen, "BATTLE");
    }

    private void setupActionCard(JPanel card, String title, Abilities actionType) {
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 3),
                new EmptyBorder(8, 12, 8, 12)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 14f));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));

        JLabel infoLbl = new JLabel();
        infoLbl.setFont(infoLbl.getFont().deriveFont(Font.PLAIN, 11f));
        infoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(infoLbl);

        // Hover and Click effects
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (card.isEnabled()) doAction(actionType);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (card.isEnabled()) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.RED, 3),
                            new EmptyBorder(8, 12, 8, 12)));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (card.isEnabled()) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.DARK_GRAY, 3),
                            new EmptyBorder(8, 12, 8, 12)));
                }
            }
        });

        card.putClientProperty("titleLbl", titleLbl);
        card.putClientProperty("infoLbl", infoLbl);
    }

    // Advance queue to next living character and refresh UI.
    private void nextTurn() {
        if (engine.isBattleOver()) { showEndScreen(); return; }

        GameCharacter actor = engine.beginTurn();
        if (actor == null) { showEndScreen(); return; }

        GameCharacter.setLogger(msg -> battleLog.append(msg + "\n"));
        turnLabel.setText("It's " + actor.getName() + "'s turn!");

        selectedTarget = engine.defaultTarget();
        
        // --- UPDATE ACTION CARD TEXT ---
        updateActionCards(actor);

        setActionButtonsEnabled(true);
        refreshArena();
    }

    private void updateActionCards(GameCharacter actor) {
        // 1. Basic Attack (No cost)
        int basicDmg = actor.calculateDamage(Abilities.BASIC_ATTACK);
        setCardText(basicCard, "Deals " + basicDmg + " DMG", true, Color.BLACK);

        // 2. Skill (Costs 1 Skill Point)
        int skillDmg = actor.calculateDamage(Abilities.SKILL);
        boolean canSkill = actor.getSkillPoints() > 0;
        String skillText = "Deals " + skillDmg + " DMG\n(Costs 1 SP)";
        setCardText(skillCard, skillText, canSkill, canSkill ? Color.BLACK : Color.RED);

        // 3. Ultimate (Costs Energy)
        int ultDmg = actor.calculateDamage(Abilities.ULTIMATE);
        boolean canUlt = actor.getEnergy() >= actor.getUltimateCost();
        String ultText = "Deals " + ultDmg + " DMG\n(Costs " + actor.getUltimateCost() + " Energy)";
        setCardText(ultimateCard, ultText, canUlt, canUlt ? Color.BLACK : Color.RED);

        // 4. Defend (Costs 1 Skill Point)
        boolean canDefend = actor.getSkillPoints() > 0 && actor.getDefendCooldown() == 0;
        String defendText = actor.getDefendCooldown() > 0 
                ? "On Cooldown (" + actor.getDefendCooldown() + " turns)" 
                : "Blocks next attack\n(Costs 1 SP)";
        setCardText(defendCard, defendText, canDefend, canDefend ? Color.BLACK : Color.RED);
    }

    private void setCardText(JPanel card, String infoText, boolean enabled, Color textColor) {
        JLabel infoLbl = (JLabel) card.getClientProperty("infoLbl");
        JLabel titleLbl = (JLabel) card.getClientProperty("titleLbl");

        String htmlText = "<html><div style='text-align: center;'>" 
                + infoText.replace("\n", "<br>") + "</div></html>";
        infoLbl.setText(htmlText);
        infoLbl.setForeground(textColor);

        card.setEnabled(enabled);
        
        if (enabled) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.setBackground(UIManager.getColor("Panel.background"));
            titleLbl.setForeground(Color.BLACK);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY, 3),
                    new EmptyBorder(8, 12, 8, 12)));
        } else {
            card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            card.setBackground(new Color(240, 240, 240)); // Slightly grayed out
            titleLbl.setForeground(Color.GRAY);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3),
                    new EmptyBorder(8, 12, 8, 12)));
        }
    }

    private void doAction(Abilities action) {
        GameCharacter target = this.selectedTarget; 
        if (action != Abilities.DEFEND && target == null) {
            battleLog.append("No valid target selected!\n");
            return;
        }

        boolean success = engine.performCurrentAction(target, action);
        refreshArena();
        
        if (success) {
            autoEndTurn();
        }
    }

    private void endTurn() {
        setActionButtonsEnabled(false);
        engine.endTurn();
        if (engine.isBattleOver()) showEndScreen();
        else nextTurn();
    }

    private void autoEndTurn() { endTurn(); }

    private void setActionButtonsEnabled(boolean on) {
        if (!on) {
            basicCard.setEnabled(false);
            skillCard.setEnabled(false);
            ultimateCard.setEnabled(false);
            defendCard.setEnabled(false);
            endTurnBtn.setEnabled(false);
        } else {
            endTurnBtn.setEnabled(true);
        }
    }

    private void refreshArena() {
        arenaPanel.removeAll();
        for (GameCharacter c : engine.getRoster()) {
            arenaPanel.add(buildCharacterCard(c));
        }
        arenaPanel.revalidate();
        arenaPanel.repaint();
    }

    private JPanel buildCharacterCard(GameCharacter c) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);

        int borderThickness = 3;
        Color borderColor = Color.DARK_GRAY;
        
        if (!c.isAlive()) {
            borderColor = Color.BLACK;
        } else if (c == selectedTarget) {
            borderColor = Color.RED;      
            borderThickness = 5;           
        } else if (engine.getCurrent() == c) {
            borderColor = new Color(218, 165, 32);
            borderThickness = 4;
        }

        if (engine.getCurrent() == c) {
            card.setBackground(new Color(255, 250, 205)); 
        } else {
            card.setBackground(UIManager.getColor("Panel.background"));
        }

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, borderThickness),
                new EmptyBorder(8, 8, 8, 8)));

        // Content Structure with Class Name
        String className = c.getClass().getSimpleName();
        JLabel nameLbl = new JLabel("<html>" + c.getName() + " <font color='gray'>[" + className + "]</font></html>");
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 16f));
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));

        int pct = c.getMaxHealth() == 0 ? 0
                : Math.max(0, (int) (100.0 * c.getHealth() / c.getMaxHealth()));
        JProgressBar hpBar = new JProgressBar(0, 100);
        hpBar.setValue(pct);
        hpBar.setStringPainted(true);
        hpBar.setString("HP " + Math.max(0, c.getHealth()) + "/" + c.getMaxHealth());
        hpBar.setForeground(pct > 50 ? new Color(60, 160, 60)
                           : pct > 20 ? new Color(200, 170, 40)
                                      : new Color(200, 60, 60));
        hpBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        hpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        card.add(hpBar);
        card.add(Box.createVerticalStrut(4));

        card.add(leftLabel("Energy: " + c.getEnergy() + "/" + c.getUltimateCost()));
        card.add(leftLabel("Skill Pts: " + c.getSkillPoints()));
        card.add(leftLabel("Shield: " + c.getShieldPoints()));
        if (c.getDefendCooldown() > 0)
            card.add(leftLabel("Defend CD: " + c.getDefendCooldown()));
        if (!c.isAlive())
            card.add(leftLabel("\u2620 DEFEATED"));

        if (c.isAlive() && engine.getCurrent() != null && c != engine.getCurrent()) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedTarget = c;
                    refreshArena();
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (c != selectedTarget) {
                        card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.RED, 3),
                                new EmptyBorder(8, 8, 8, 8)));
                    }
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (c != selectedTarget) {
                        card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.DARK_GRAY, 3),
                                new EmptyBorder(8, 8, 8, 8)));
                    }
                }
            });
        }

        return card;
    }

    private JLabel leftLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    //  END SCREEN
    private void showEndScreen() {
        GameCharacter w = engine.getWinner();
        String msg = (w != null)
                ? "<html><h1>" + w.getName() + " WINS!</h1></html>"
                : "<html><h1>Battle ended in a draw.</h1></html>";

        JOptionPane.showMessageDialog(this, msg, "Battle Over",
                JOptionPane.INFORMATION_MESSAGE);

        turnLabel.setText(w != null ? w.getName() + " is victorious!" : "Draw!");
        setActionButtonsEnabled(false);
        refreshArena();

        int choice = JOptionPane.showConfirmDialog(this,
                "Return to Character Manager?", "Battle Over",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            cards.show(root, "MANAGER");
            java.util.List<GameCharacter> dead = new ArrayList<>();
            for (GameCharacter c : engine.getRoster())
                if (!c.isAlive()) dead.add(c);
            for (GameCharacter c : dead) {
                engine.removeCharacter(c.getName());
                rosterModel.removeElement(c);
            }
        }
    }

    //  Roster renderer
    private static class RosterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            if (value instanceof GameCharacter gc) {
                setText(gc.getName() + "  —  " + gc.getClass().getSimpleName()
                        + "  (HP " + gc.getHealth() + "/" + gc.getMaxHealth() + ")");
            }
            return this;
        }
    }
}
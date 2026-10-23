# turn-based-combat-with-queues
A Java project using Queues to simulate a turn based game with Java Swing.

# Additional Features:
- Two new classes: Archer and Assassin.
- Health, Power, Energy, Skill Points, and Shield Points stats for Characters.
- Four different abilities to use in battle: Basic Attack, Skill, Defend, and Ultimate.
- Simulated turn-based battle with two or more characters.
- Implementation of Queues to handle character turn order.
- Option to Add and Remove Characters on the roster.
- Class exclusive abilities.

# The Battle System
- Each character has a set amount of HP, the goal is to use attacks to deplete the enemy HP.
- The Fight is turn-based, each character will have one chance to use a move.
- Skills require Skill Points, they can be gained through using Basic Attacks.
- Ultimate moves are powerful, they can be used once the maximum Energy is reached.
- Energy can be gained through using Basic Attacks, Skills, and successfully Defending.
- Defend allows a character to block incoming damage for one turn, this has a cooldown of two turns.
- Shield Points allow characters to absorb incoming damage without depleting their HP.

# Character Classes
| Type | Details |
| --- | --- |
| Warrior | High-HP character that gains shield points after using a Skill. |
| Mage | Decent-DMG character with a high Energy Cost, using a skill gives them 20 energy. |
| Archer | High-DMG character with huge attack but low Max-HP. |
| Assassin | High-DMG character that increases power after each turn. |

# How to use the program:
**1. Create a Character**<br>
- Type in the name of your character and choose their respective class. 
- Click the 'Create' button to deploy them on the roster.
- Place in as many characters you need and delete them as you wish.
- NOTE: Each class has different stats and skills.

**2. Start the Battle**<br>
- Start the battle and choose a target to attack, each characters' stats are displayed and multiple abilities can be used.
- The Battle Log will record each turn by a character and some reminders whenever an error occurs.

**3. Strategize and Win the Battle**
- Plan your attacks and defend accordingly, the simulation will end once one character remains standing in the battlefield.

# Project Files
| File | Purpose |
| --- | --- |
| `Main.java` | Main class that contains the start of the program. |
| `BattleEngine.java` | Contains the queue system and rules for the turn-based system. |
| `GameCharacter.java` | Abstract base class for the characters. |
| `GameGUI.java` | Contains the components for the User Interface. |
| `Warrior.java`, `Mage.java`, `Archer.java`, `Assassin.java` | Five character classes and rules for their skill multipliers and taking damage. |

package mainPackage;
import java.util.function.Consumer;

enum Abilities {
    BASIC_ATTACK,
    SKILL,
    ULTIMATE,
    DEFEND
}

abstract class GameCharacter {
    protected String name;
    protected int health;
    protected int maxHealth;
    protected int power;
    protected int energy = 0;
    protected int skill_points;
    protected int shield_points;
    protected boolean isDefending = false;
    protected int defendCooldown = 0;
    
    protected static Consumer<String> logger = System.out::println;

    public static void setLogger(Consumer<String> logSink) {
        logger = (logSink == null) ? System.out::println : logSink;
    }

    protected void log(String msg) { logger.accept(msg); }

    public GameCharacter(String name, int health, int power, int skill_points, int shield_points) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.power = power;
        this.skill_points = skill_points;
        this.shield_points = shield_points;
     
        this.energy = getUltimateCost() / 2; 
    }

    //Getters for GUI and game Engine
    public String getName()          { return name; }
    public int getHealth()           { return health; }
    public int getMaxHealth()        { return maxHealth; }
    public int getPower()            { return power; }
    public int getEnergy()           { return energy; }
    public int getSkillPoints()      { return skill_points; }
    public int getShieldPoints()     { return shield_points; }
    public int getDefendCooldown()   { return defendCooldown; }
    public boolean isDefending()     { return isDefending; }
    public boolean isAlive()         { return health > 0; }

    public void showName() { log("Character: " + name); }

    public void displayStatus() {
        String cooldownStatus = (defendCooldown > 0)
                ? " (Defend Cooldown: " + defendCooldown + " turns)"
                : " (Defend Ability: Ready)";
        log("HP: " + health + "\nPower: " + power +
            "\nEnergy: " + energy + "/" + getUltimateCost() +
            "\nShield Points: " + shield_points + cooldownStatus);
    }
    
    @Override
    public String toString() {
        return name;
    }

    public void startTurn() {
        this.isDefending = false;
        if (this.defendCooldown > 0) this.defendCooldown--;
    }

    protected void gainEnergy(int amount) {
        this.energy = Math.min(getUltimateCost(), this.energy + amount);
    }

    public boolean performAction(GameCharacter target, Abilities action) {
        if (action == null) return false;

        switch (action) {
            case DEFEND:
                if (this.defendCooldown > 0) {
                    log(this.name + " [DEFEND] ability is on cooldown.");
                    return false;
                }
                if (this.skill_points <= 0) {
                    log(this.name + " has no skill points to defend!");
                    return false;
                }
                this.skill_points--;
                this.isDefending = true;
                this.defendCooldown = 2;
                log(this.name + " uses [DEFEND], the next attack will be blocked.");
                this.gainEnergy(20);
                return true;

            case SKILL:
                if (this.skill_points == 0) {
                    log(this.name + " is out of skill points! [SKILL] cannot be used.");
                    return false;
                }
                this.skill_points--;
                this.gainEnergy(15);

                if (this instanceof Warrior) {
                    this.shield_points += 100;
                }
                
                if (this instanceof Mage) {
                	this.gainEnergy(20);
                }
                
                if (this instanceof Assassin) {
                	this.power += 5;
                }
                
                break;

            case BASIC_ATTACK:
                if (this.skill_points < 5) {
                    this.skill_points++;
                }
                this.gainEnergy(10);
                break;

            case ULTIMATE:
                int uniqueCost = this.getUltimateCost();
                if (this.energy < uniqueCost) {
                    log("Not enough energy for Ultimate move.");
                    return false;
                }
                this.energy -= uniqueCost;
                break;
        }

        String actionMsg = switch (action) {
            case BASIC_ATTACK -> "   " + name + " uses Basic Attack!";
            case SKILL        -> "   " + name + " uses [SKILL]!";
            case ULTIMATE     -> "   " + name + " uses [ULTIMATE]!";
            default           -> "";
        };
        log(actionMsg);

        int damageDealt = this.calculateDamage(action);
        target.takeDamage(damageDealt, this, action);
        return true;
    }
   
    public abstract int calculateDamage(Abilities action);
    public abstract int getUltimateCost();
    public abstract void takeDamage(int incomingDamage, GameCharacter attacker, Abilities action);
}
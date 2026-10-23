package mainPackage;
class Mage extends GameCharacter {
    public Mage(String name) {
        super(name, 1000, 100, 2, 0);
    }

    @Override public int getUltimateCost() { return 240; }

    @Override
    public int calculateDamage(Abilities action) {
        switch (action) {
            case BASIC_ATTACK:
                return this.power;
            case SKILL:
                return (int)(this.power * 1.75);
            case ULTIMATE:
                return (int)(this.power * 2.5);
            default:
                return 0;
        }
    }

    @Override
    public void takeDamage(int incomingDamage, GameCharacter attacker, Abilities action) {
        if (this.isDefending) {
            log(" [BLOCKED] " + name + " damage is nullified!");
            return;
        }
        
        if (this.shield_points > 0) {
            int absorbed = Math.min(this.shield_points, incomingDamage);
            this.shield_points -= absorbed;
            incomingDamage -= absorbed;
            
            if (this.shield_points == 0) {
                log("   " + name + "'s shield broke!");
            }
        } 
        
        int finalDamage = Math.max(0, incomingDamage);
        this.health -= finalDamage;
        log("   " + name + " receives " + finalDamage + " DMG.");
    }
}
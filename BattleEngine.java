package mainPackage;

import java.util.*;

/**
 * Manages turn order with Queues
 *  - character at front of poll() has their turn
 *  - while they're alive, offer() to give back their queue
 *  - characters who died will be removed
 */

public class BattleEngine {

    private final Queue<GameCharacter> turnQueue = new LinkedList<>();
    private final Map<String, GameCharacter> roster = new LinkedHashMap<>();
    private GameCharacter current;
    private boolean battleOver = false;
    private GameCharacter winner;

    public void addCharacter(GameCharacter c) {
        roster.put(c.getName(), c);
        turnQueue.offer(c);
    }

    public boolean removeCharacter(String name) {
        GameCharacter c = roster.get(name);
        if (c == null) return false;
        roster.remove(name);
        turnQueue.remove(c);
        return true;
    }

    public Collection<GameCharacter> getRoster() { return roster.values(); }

    public GameCharacter getCurrent() { return current; }
    public boolean isBattleOver()    { return battleOver; }
    public GameCharacter getWinner() { return winner; }

    public boolean canStartBattle() {
        return turnQueue.size() >= 2;
    }

    //Returns the character who has their turn
    public GameCharacter beginTurn() {
        if (battleOver) return null;
        if (turnQueue.isEmpty()) return null;

        current = turnQueue.peek();        
        current.startTurn();
        return current;
    }

    //Character turn is finished, re-queues if they're still alive
    public void endTurn() {
        if (current == null) return;

        GameCharacter actor = turnQueue.poll();
        if (actor == null) return;

        turnQueue.removeIf(c -> !c.isAlive());

        if (actor.isAlive()) {
            turnQueue.offer(actor);
        }

        if (turnQueue.size() <= 1) {
            battleOver = true;
            winner = turnQueue.peek();
            current = null;
        } else {
            current = null;
        }
    }

    public boolean performCurrentAction(GameCharacter target, Abilities action) {
        if (current == null || battleOver) return false;
        return current.performAction(target, action);
    }

    public List<GameCharacter> getLiving() {
        List<GameCharacter> living = new ArrayList<>();
        for (GameCharacter c : turnQueue) if (c.isAlive()) living.add(c);
        return living;
    }

    public GameCharacter defaultTarget() {
        for (GameCharacter c : turnQueue) {
            if (c != current && c.isAlive()) return c;
        }
        return null;
    }
    
    public void reset() {
        battleOver = false;
        winner = null;
        current = null;
        
        turnQueue.removeIf(c -> !c.isAlive());
    }
}
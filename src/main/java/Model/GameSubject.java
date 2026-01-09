package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject class for the Observer design pattern.
 * Manages a list of observers and notifies them when the game state changes.
 */
public class GameSubject {
    private final List<GameObserver> observers = new ArrayList<>();
    private GameStateData currentState;

    /**
     * Registers an observer to receive state change notifications.
     * @param observer the observer to register
     */
    public void registerObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer from the notification list.
     * @param observer the observer to remove
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers of a state change.
     * This method should be called whenever the game state (score or level) changes.
     * @param newState the updated game state
     */
    public void notifyObservers(GameStateData newState) {
        this.currentState = newState;
        for (GameObserver observer : observers) {
            observer.update(newState);
        }
    }

    /**
     * Gets the current game state.
     * @return the current state, or null if no state has been set
     */
    public GameStateData getCurrentState() {
        return currentState;
    }
}


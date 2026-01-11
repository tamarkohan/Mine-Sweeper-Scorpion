# DP2 – Observer Pattern (Iteration 03)

## 1. What problem did we have before Observer?

Before applying the Observer pattern, the UI components (score, lives, status labels) needed to be updated manually in multiple places throughout the code. This caused several issues:

- **Duplicated code**: The same UI update logic (e.g., `lblScore.setText("SCORE: " + score)`) was repeated in multiple methods like `updateStatus()`, `handleMoveMade()`, and after game actions.
- **Tight coupling**: The View layer (`GamePanel`) was directly calling controller methods to fetch state and update UI elements, creating a dependency where the View needed to know when and how to refresh itself.
- **Inconsistency risk**: If a developer forgot to call `updateStatus()` after a state change, the UI would show stale data, leading to bugs and user confusion.
- **Maintenance burden**: Adding new UI components that depend on game state required updating multiple places in the code, making the system harder to maintain and extend.

## 2. Why Observer is a good fit here

The Observer pattern fits perfectly because our game has a **single source of truth** (the game state in the Model layer) while **multiple UI components** depend on it. The pattern allows:

- **Decoupled updates**: The Model doesn't need to know about specific UI components. It simply notifies all registered observers when state changes.
- **Consistency**: All UI components receive the same state update simultaneously, ensuring they always display synchronized information.
- **Extensibility**: New UI components can subscribe to state changes without modifying existing Model or Controller code.
- **Single responsibility**: The Model focuses on game logic, while UI components handle their own rendering based on state notifications.

## 3. How it is implemented in our project

**Subject:** `Model.GameSubject` – manages a list of observers and notifies them whenever the game state changes. It provides methods to register, remove, and notify observers.

**Observer:** `View.GamePanel` – implements the `GameObserver` interface and updates UI elements (score label, lives label, hearts, etc.) inside its `update(GameStateData state)` method.

**Trigger/Wrapper:** `Controller.GameController` – acts as the bridge between View and Model. After game actions (like revealing a cell), it calls `notifyStateChange()` on the subject, which then notifies all registered observers.

### 3.1 Observer interface (GameObserver)

```java
public interface GameObserver {
    void update(GameStateData state);
}
```

The `GameStateData` is a simple data transfer object (DTO) containing the current game state: score, lives, game state, current turn, etc.

### 3.2 Subject implementation (GameSubject)

```java
public class GameSubject {
    private List<GameObserver> observers = new ArrayList<>();
    
    public void registerObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(GameStateData state) {
        for (GameObserver observer : observers) {
            observer.update(state);
        }
    }
}
```

### 3.3 Observer implementation (GamePanel.update)

```java
@Override
public void update(GameStateData state) {
    lblScore.setText("SCORE: " + state.getScore());
    lblLives.setText("LIVES: " + state.getLives() + "/" + state.getMaxLives());
    // Update hearts, mines left, etc.
    updateHearts();
    revalidate();
    repaint();
}
```

## 4. Concrete example flow

When the player reveals a cell, the following sequence occurs:

1. **User action**: Player clicks a cell → `BoardPanel` calls `GameController.revealCellUI()`
2. **State change**: `GameController` delegates to `Board.revealCell()`, which updates the game state (score increases, cell is revealed)
3. **Notification trigger**: `GameController` calls `notifyStateChange()` after the state change
4. **Subject notification**: `GameSubject.notifyObservers(state)` iterates through all registered observers
5. **UI update**: `GamePanel.update(state)` is called automatically, updating `lblScore` and other UI components

This flow ensures that whenever the score changes in the Model, all UI components are automatically updated without the View needing to manually poll or refresh.

## 5. Result / Benefits

- **Centralized updates**: UI updates are centralized via the `update(state)` method, eliminating duplicated update code.
- **Reduced coupling**: The Model no longer depends on View classes, and the View doesn't need to know when to refresh—it's notified automatically.
- **Easier maintenance**: Adding new UI components that display game state only requires implementing `GameObserver` and registering with the subject.
- **Consistency**: All observers receive the same state snapshot, preventing inconsistencies between different UI elements.
- **Testability**: The Observer pattern makes it easier to test state changes by creating mock observers that verify they receive correct notifications.


package Model;

import java.time.LocalDateTime;

/**
 * Represents a single finished game record for the Game History screen.
 */
public class GameHistoryEntry {

    private final LocalDateTime timestamp;
    private final String player1Name;
    private final String player2Name;
    private final String difficulty;
    private final String result;       // "WON" / "LOST"
    private final int finalScore;
    private final int livesLeft;
    private final long durationSeconds;

    // question-related stats for more advanced history/players view
    private final int totalQuestions;
    private final int correctAnswers;

    public GameHistoryEntry(LocalDateTime timestamp,
                            String player1Name,
                            String player2Name,
                            String difficulty,
                            String result,
                            int finalScore,
                            int livesLeft,
                            long durationSeconds,
                            int totalQuestions,
                            int correctAnswers) {
        this.timestamp = timestamp;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.difficulty = difficulty;
        this.result = result;
        this.finalScore = finalScore;
        this.livesLeft = livesLeft;
        this.durationSeconds = durationSeconds;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
    }

    public LocalDateTime getTimestamp()      { return timestamp; }
    public String getPlayer1Name()           { return player1Name; }
    public String getPlayer2Name()           { return player2Name; }
    public String getDifficulty()            { return difficulty; }
    public String getResult()                { return result; }
    public int getFinalScore()               { return finalScore; }
    public int getLivesLeft()                { return livesLeft; }
    public long getDurationSeconds()         { return durationSeconds; }
    public int getTotalQuestions()           { return totalQuestions; }
    public int getCorrectAnswers()           { return correctAnswers; }

    public double getAccuracy() {
        if (totalQuestions <= 0) return 0.0;
        return (correctAnswers * 100.0) / totalQuestions;
    }

    public String getFormattedAccuracy() {
        if (totalQuestions <= 0) return "-";
        return String.format("%.0f%%", getAccuracy());
    }

    public String getFormattedDuration() {
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

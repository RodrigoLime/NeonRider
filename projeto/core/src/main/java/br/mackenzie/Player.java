package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Player {

    // --- Constants ---
    public static final int COMBO_THRESHOLD = 10;
    public static final int MAX_MULTIPLIER = 4;
    public static final int POINTS_PER_NOTE = 10;

    private static final float STATE_RESET_TIME = 0.1f;

    private enum PlayerState {
        IDLE,
        LEFT,
        RIGHT
    }

    // --- Player State ---
    private int score = 0;
    private int combo = 0;
    private int multiplier = 1;
    private int hitsSinceLastMultiplier = 0;

    private PlayerState visualState = PlayerState.IDLE;
    private float stateTimer = 0;
    private Texture idleTexture;
    private Texture leftTexture;
    private Texture rightTexture;

    // --- Feedback State ---
    private String hitFeedback = "";
    private float feedbackTimer = 0;
    private Color hitFeedbackColor = Color.WHITE;

    public Player(Texture idleTexture, Texture leftTexture, Texture rightTexture) {
        this.idleTexture = idleTexture;
        this.leftTexture = leftTexture;
        this.rightTexture = rightTexture;
    }

    /**
     * Updates the player's internal state, like the feedback timer.
     * Call this every frame in GameScreen's updateLogic().
     */
    public void update(float delta) {
        if (feedbackTimer > 0) {
            feedbackTimer -= delta;
            if (feedbackTimer <= 0) {
                hitFeedback = "";
            }
        }

        if (stateTimer > 0) {
            stateTimer -= delta;
            if (stateTimer <= 0) {
                visualState = PlayerState.IDLE;
            }
        }
    }

    /**
     * Call this when the player successfully hits a note.
     */
    public void processHit() {
        combo++;
        hitsSinceLastMultiplier++;
        if (hitsSinceLastMultiplier >= COMBO_THRESHOLD && multiplier < MAX_MULTIPLIER) {
            multiplier++;
            hitsSinceLastMultiplier = 0;
        } else if (multiplier == MAX_MULTIPLIER) {
            hitsSinceLastMultiplier = Math.min(hitsSinceLastMultiplier, COMBO_THRESHOLD);
        }
        score += (POINTS_PER_NOTE * multiplier);
        setHitFeedback("ACERTOU!", Color.GREEN);
    }

    /**
     * Call this when the player misses (either by pressing at the wrong time
     * or by a note passing the hit zone).
     * @param isNotePass True if a note passed the zone, false if the player just pressed 'ERROU'
     */
    public void processMiss(boolean isNotePass) {
        if (isNotePass) {
            score = (score >= 5) ? score - 5 : 0;
            setHitFeedback("PERDEU!", Color.GRAY);
        } else {
            score = (score >= 2) ? score - 2 : 0;
            setHitFeedback("ERROU!", Color.RED);
        }
        combo = 0;
        multiplier = 1;
        hitsSinceLastMultiplier = 0;
    }

    private void setHitFeedback(String text, Color color) {
        this.hitFeedback = text;
        this.hitFeedbackColor = color;
        this.feedbackTimer = 0.5f;
    }

    public void setPressedState(boolean isLeft) {
        visualState = isLeft ? PlayerState.LEFT : PlayerState.RIGHT;
        stateTimer = STATE_RESET_TIME;
    }

    public Texture getCurrentTexture() {
        switch (visualState) {
            case LEFT:
                return leftTexture;
            case RIGHT:
                return rightTexture;
            case IDLE:
            default:
                return idleTexture;
        }
    }

    // --- Getters (for rendering) ---
    public int getScore() { return score; }
    public int getCombo() { return combo; }
    public int getMultiplier() { return multiplier; }
    public String getHitFeedback() { return hitFeedback; }
    public Color getFeedbackColor() { return hitFeedbackColor; }
    public boolean isFeedbackActive() { return feedbackTimer > 0; }


    public float getMultiplierProgress() {
        if (multiplier >= MAX_MULTIPLIER) return 1.0f;
        return (float) hitsSinceLastMultiplier / (float) COMBO_THRESHOLD;
    }
}

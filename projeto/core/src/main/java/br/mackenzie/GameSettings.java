package br.mackenzie;

import com.badlogic.gdx.Input;

public class GameSettings {


    public enum Difficulty {
        EASY(750),
        MEDIUM(500),
        HARD(300);

        public final long spawnInterval;

        Difficulty(long interval) {
            this.spawnInterval = interval;
        }
    }


    public int keyLeft = Input.Keys.Z;
    public int keyRight = Input.Keys.X;
    public float volume = 0.5f; //
    public Difficulty difficulty = Difficulty.MEDIUM;

}

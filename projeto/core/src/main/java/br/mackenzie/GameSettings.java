package br.mackenzie;

import com.badlogic.gdx.Input;

public class GameSettings {


    public enum ScreenMode {
        WINDOWED,      // Janela normal
        BORDERLESS,    // Janela sem bordas (Tela cheia em janela)
        FULLSCREEN     // Tela cheia exclusiva
    }

    public enum Difficulty {


        EASY(631.58f, "music1.ogg", 0.0f, 60.0f, true, "background-1.png"),  // 95 BPM
        MEDIUM(437.96f, "music2.ogg", 0.0f, 123.0f, false, "background-2.png"), // 137 BPM
        HARD(365.85f, "music3.ogg", 0.0f, 148.0f, false, "background-3.png"); // 164 BPM

        public final float spawnInterval;
        public final String musicFile;
        public final float musicStartTime;
        public final float musicEndTime;
        public final boolean shouldLoop;
        public final String backgroundFile;

        Difficulty(float interval, String file, float start, float end, boolean loop, String bg) {
            this.spawnInterval = interval;
            this.musicFile = file;
            this.musicStartTime = start;
            this.musicEndTime = end;
            this.shouldLoop = true;
            this.backgroundFile = bg;
        }

    }

    public int keyLeft = Input.Keys.LEFT;
    public int keyRight = Input.Keys.RIGHT;
    public float volume = 0.5f;
    public Difficulty difficulty = Difficulty.MEDIUM;
    public ScreenMode screenMode = ScreenMode.WINDOWED;
}

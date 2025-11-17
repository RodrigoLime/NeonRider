package br.mackenzie;

import com.badlogic.gdx.Input;

public class GameSettings {

    
    public enum ScreenMode {
        WINDOWED,      // Janela normal
        BORDERLESS,    // Janela sem bordas (Tela cheia em janela)
        FULLSCREEN     // Tela cheia exclusiva
    }

    public enum Difficulty {
      

        EASY(750, "musica_easy_see_you_again.ogg", 10.0f, 210.0f),  // 80 BPM
        MEDIUM(500, "musica_medium_blinding_lights.ogg", 2.5f, 190.0f), // 120 BPM
        HARD(300, "musica_hard_riot.ogg", 14.0f, 180.0f); // 200 BPM

        public final long spawnInterval;
        public final String musicFile;
        public final float musicStartTime;
        public final float musicEndTime;

        Difficulty(long interval, String file, float start, float end) {
            this.spawnInterval = interval;
            this.musicFile = file;
            this.musicStartTime = start;
            this.musicEndTime = end;
        }
       
    }

    public int keyLeft = Input.Keys.Z;
    public int keyRight = Input.Keys.X;
    public float volume = 0.5f; 
    public Difficulty difficulty = Difficulty.MEDIUM;
    public ScreenMode screenMode = ScreenMode.WINDOWED; 
}

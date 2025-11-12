package br.mackenzie;

import com.badlogic.gdx.Input;

public class GameSettings {

    // NOVO: Enum para o modo de tela
    public enum ScreenMode {
        WINDOWED,      // Janela normal
        BORDERLESS,    // Janela sem bordas (Tela cheia em janela)
        FULLSCREEN     // Tela cheia exclusiva
    }

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
    public float volume = 0.5f; 
    public Difficulty difficulty = Difficulty.MEDIUM;
    public ScreenMode screenMode = ScreenMode.WINDOWED; // NOVO: Define o padrão
}
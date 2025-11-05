package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {

    // ... (Classe 'Note' permanece igual) ...
    private static class Note {
        enum NoteType { LEFT, RIGHT }
        Rectangle rect;
        NoteType type;
        boolean hit = false;

        public Note(float x, float y, NoteType type) {
            this.rect = new Rectangle(x, y, 50, 20); // Largura 50, Altura 20 (virtual)
            this.type = type;
        }
        public void update(float delta, float speed) {
            rect.y -= speed * delta;
        }
    }

    // Referências globais
    private final Main game;
    private final GameSettings settings;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;

    // Assets específicos desta tela
    private Music musica;
    private Texture motoTexture;

    // Posições (em coordenadas virtuais 640x480)
    private float laneLeftX, laneRightX, laneWidth = 50;
    private Rectangle hitZoneLeft, hitZoneRight;
    private float hitZoneY = 100;

    // Lógica do Jogo
    private Array<Note> notes;
    private long lastNoteTime;
    private float noteSpeed = 300.0f;
    private long spawnInterval;
    
    // Não precisamos mais dos fatores de resize
    // private float resizeXFactor = Gdx.graphics.getWidth() / (float)SettingsScreen.GAME_WIDTH;
    // private float resizeYFactor = Gdx.graphics.getHeight() / (float)SettingsScreen.GAME_HEIGHT;

    private String feedback = "Vamos começar!";
    private Color feedbackColor = Color.WHITE;
    private int score = 0;

    private int noteSpawnCount = 0;

    public GameScreen(final Main game) {
        // Pega os objetos globais da classe 'main'
        this.game = game;
        this.settings = game.settings;
        this.batch = game.batch;
        this.font = game.font;
        this.shapeRenderer = game.shapeRenderer;

        // USA A CONFIGURAÇÃO DE DIFICULDADE
        this.spawnInterval = settings.difficulty.spawnInterval;

        // Carrega assets desta tela
        try {
            motoTexture = new Texture(Gdx.files.internal("neon_rider_idle.png"));
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar neon_rider_idle.png", e); }

        try {
            musica = Gdx.audio.newMusic(Gdx.files.internal("music.ogg"));
            musica.setLooping(true);
            // USA A CONFIGURAÇÃO DE VOLUME
            musica.setVolume(settings.volume);
        } catch (Exception e) { Gdx.app.error("Audio", "Nao foi possivel carregar musica.mp3", e); }

        // Configura pistas usando coordenadas virtuais
        float virtualWidth = SettingsScreen.GAME_WIDTH;
        laneLeftX = virtualWidth / 2f - laneWidth * 1.5f;
        laneRightX = virtualWidth / 2f + laneWidth * 0.5f;
        hitZoneLeft = new Rectangle(laneLeftX, hitZoneY, laneWidth, 20);
        hitZoneRight = new Rectangle(laneRightX, hitZoneY, laneWidth, 20);

        notes = new Array<>();
        lastNoteTime = TimeUtils.millis();
    }

    @Override
    public void show() {
        // Toca a música quando a tela aparece
        if (musica != null) {
            musica.play();
        }
        // Garante que o InputProcessor das telas de menu não está ativo
        Gdx.input.setInputProcessor(null);
    }

    private void spawnNote() {
        // Mantém a lógica de sempre alternar
        Note.NoteType type = (noteSpawnCount % 2 == 0) ? Note.NoteType.LEFT : Note.NoteType.RIGHT;
        float x = (type == Note.NoteType.LEFT) ? laneLeftX : laneRightX;
        // Spawna no topo da tela virtual
        notes.add(new Note(x, SettingsScreen.GAME_HEIGHT, type));
        lastNoteTime = TimeUtils.millis();

        noteSpawnCount++;
    }

    private void updateLogic(float delta) {
        // 1. Spawna notas
        if (TimeUtils.millis() - lastNoteTime > spawnInterval) {
            spawnNote();
        }

        // 2. Movimenta e remove notas
        for (int i = notes.size - 1; i >= 0; i--) {
            Note note = notes.get(i);
            note.update(delta, noteSpeed);

            if (note.rect.y < hitZoneY - note.rect.height && !note.hit) {
                feedback = "ERROU!";
                feedbackColor = Color.RED;
                score = (score >= 5) ?  score - 5 : 0;
                notes.removeIndex(i);
            }
        }

        // 3. Verifica input
        handleInput();

        // 4. Botão de emergência para sair (ESC)
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game)); // Volta ao menu
        }
    }

    private void handleInput() {
        // USA AS TECLAS DAS CONFIGURAÇÕES
        if (Gdx.input.isKeyJustPressed(settings.keyLeft)) {
            checkHit(Note.NoteType.LEFT, hitZoneLeft);
        }
        if (Gdx.input.isKeyJustPressed(settings.keyRight)) {
            checkHit(Note.NoteType.RIGHT, hitZoneRight);
        }
    }

    private void checkHit(Note.NoteType type, Rectangle hitZone) {
        boolean hitSomething = false;
        for (int i = notes.size - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (note.type == type && !note.hit) {
                // Se a nota está colidindo com a zona de acerto
                if (note.rect.overlaps(hitZone)) {
                    feedback = "ACERTOU!";
                    feedbackColor = Color.GREEN;
                    score += 10;
                    note.hit = true;
                    notes.removeIndex(i);
                    hitSomething = true;
                    break;
                }
            }
        }
        if (!hitSomething) {
            feedback = "Errou a batida!";
            feedbackColor = Color.GRAY;
            score = (score >= 2) ? score - 2 : 0;
        }
    }

    @Override
    public void render(float delta) {
        updateLogic(delta);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // A câmera já está sendo aplicada na classe Main

        // Desenha Zonas e Notas (ShapeRenderer)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 0.3f); // Verde
        shapeRenderer.rect(hitZoneLeft.x, hitZoneLeft.y, hitZoneLeft.width, hitZoneLeft.height);
        shapeRenderer.setColor(1, 0, 0, 0.3f); // Vermelho
        shapeRenderer.rect(hitZoneRight.x, hitZoneRight.y, hitZoneRight.width, hitZoneRight.height);
        for (Note note : notes) {
            if (note.hit) continue;
            shapeRenderer.setColor((note.type == Note.NoteType.LEFT) ? Color.CYAN : Color.MAGENTA);
            shapeRenderer.rect(note.rect.x, note.rect.y, note.rect.width, note.rect.height);
        }
        shapeRenderer.end();

        // Desenha Moto e UI (SpriteBatch)
        batch.begin();
        if (motoTexture != null) {
            // Posição da moto em coordenadas virtuais
            batch.draw(motoTexture, 255, 10);
        }
        
        // Desenha UI usando coordenadas virtuais
        font.setColor(feedbackColor);
        font.draw(batch,
                  feedback,
                  (SettingsScreen.GAME_WIDTH / 2f - 50),  // Centralizado
                  (SettingsScreen.GAME_HEIGHT - 50)); // Topo
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, 20, (SettingsScreen.GAME_HEIGHT - 20)); // Canto superior esquerdo
        font.draw(batch, "ESC para Sair",
                  (SettingsScreen.GAME_WIDTH - 120), // Canto superior direito
                  (SettingsScreen.GAME_HEIGHT - 20));
        batch.end();
    }

    @Override
    public void hide() {
        // Para a música e libera recursos ao sair da tela
        dispose();
    }

    @Override
    public void dispose() {
        // Libera os assets DESTA TELA
        if (musica != null) musica.dispose();
        if (motoTexture != null) motoTexture.dispose();
    }

    // --- Métodos obrigatórios da interface Screen ---
    // O resize agora é tratado na classe Main
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
}
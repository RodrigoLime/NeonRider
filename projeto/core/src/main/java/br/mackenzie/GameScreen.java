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

    // --- Classe Note (Interna) ---
    private static class Note {
        enum NoteType { LEFT, RIGHT }
        Rectangle rect;
        NoteType type;
        boolean hit = false;

        public Note(float x, float y, NoteType type) {
            this.rect = new Rectangle(x, y, 50, 20);
            this.type = type;
        }
        public void update(float delta, float speed) {
            rect.y -= speed * delta;
        }
    }
    // --- Fim da Classe Note ---

    // Referências globais
    private final Main game;
    private final GameSettings settings;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;

    // Assets específicos desta tela
    private Music musica;
    private Texture motoTexture;
    private Texture roadTexture;
    private Texture sceneryTexture;

    // Posições
    private float laneLeftX, laneRightX, laneWidth = 50;
    private Rectangle hitZoneLeft, hitZoneRight;
    private float hitZoneY = 100;

    // Lógica do Jogo
    private Array<Note> notes;
    private long lastNoteTime;
    private float noteSpeed = 300.0f;
    private long spawnInterval;
    private float roadScrollY = 0;

    private float sceneryScrollY = 0;
    private float scenerySpeedMultiplier = 0.2f;

    // --- LÓGICA DE PONTUAÇÃO ---
    private int score = 0;
    private int combo = 0;
    private int multiplier = 1;
    
    private final int COMBO_THRESHOLD = 10;
    private final int MAX_MULTIPLIER = 4;
    private final int POINTS_PER_NOTE = 10;

    // --- LÓGICA DE PROGRESSO DO MULTIPLICADOR ---
    private int hitsSinceLastMultiplier = 0;

    // Lógica de Feedback de Acerto
    private String hitFeedback = "";
    private float feedbackTimer = 0;
    private Color hitFeedbackColor = Color.WHITE;

    private int noteSpawnCount = 0;

    // Referências para o tamanho virtual
    private final float V_WIDTH = SettingsScreen.GAME_WIDTH;
    private final float V_HEIGHT = SettingsScreen.GAME_HEIGHT;

    public GameScreen(final Main game) {
        this.game = game;
        this.settings = game.settings;
        this.batch = game.batch;
        this.font = game.font;
        this.shapeRenderer = game.shapeRenderer;
        this.spawnInterval = settings.difficulty.spawnInterval;

        try {
            sceneryTexture = new Texture(Gdx.files.internal("background-1.png"));
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar background-1", e); }
        try {
            roadTexture = new Texture(Gdx.files.internal("neon_road.png"));
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar neon_road.png", e); }
        try {
            motoTexture = new Texture(Gdx.files.internal("neon_rider_idle.png"));
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar neon_rider_idle.png", e); }
        try {
            musica = Gdx.audio.newMusic(Gdx.files.internal("music.ogg"));
            musica.setLooping(true);
            musica.setVolume(settings.volume);
        } catch (Exception e) { Gdx.app.error("Audio", "Nao foi possivel carregar musica.ogg", e); }

        laneLeftX = V_WIDTH / 2f - laneWidth * 1.5f;
        laneRightX = V_WIDTH / 2f + laneWidth * 0.5f;
        hitZoneLeft = new Rectangle(laneLeftX, hitZoneY, laneWidth, 20);
        hitZoneRight = new Rectangle(laneRightX, hitZoneY, laneWidth, 20);

        notes = new Array<>();
        lastNoteTime = TimeUtils.millis();
    }

    @Override
    public void show() {
        if (musica != null) {
            musica.play();
        }
        Gdx.input.setInputProcessor(null);
    }

    private void setHitFeedback(String text, Color color) {
        this.hitFeedback = text;
        this.hitFeedbackColor = color;
        this.feedbackTimer = 0.5f;
    }

    private void spawnNote() {
        Note.NoteType type = (noteSpawnCount % 2 == 0) ? Note.NoteType.LEFT : Note.NoteType.RIGHT;
        float x = (type == Note.NoteType.LEFT) ? laneLeftX : laneRightX;
        notes.add(new Note(x, V_HEIGHT, type));
        lastNoteTime = TimeUtils.millis();
        noteSpawnCount++;
    }

    private void updateLogic(float delta) {
        if (feedbackTimer > 0) {
            feedbackTimer -= delta;
            if (feedbackTimer <= 0) {
                hitFeedback = "";
            }
        }

        if (TimeUtils.millis() - lastNoteTime > spawnInterval) {
            spawnNote();
        }

        for (int i = notes.size - 1; i >= 0; i--) {
            Note note = notes.get(i);
            note.update(delta, noteSpeed);

            if (note.rect.y < hitZoneY - note.rect.height && !note.hit) {
                score = (score >= 5) ?  score - 5 : 0;
                combo = 0;
                multiplier = 1;
                hitsSinceLastMultiplier = 0;
                
                setHitFeedback("PERDEU!", Color.GRAY);
                
                notes.removeIndex(i);
            }
        }

        handleInput();

        roadScrollY += delta * noteSpeed;
        sceneryScrollY += delta * noteSpeed * scenerySpeedMultiplier;


    }

    private void handleInput() {
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
                if (note.rect.overlaps(hitZone)) {
                    
                    // Lógica de Combo/Multiplicador
                    combo++;
                    hitsSinceLastMultiplier++;

                    // Se atingiu o threshold E não está no multiplicador máximo
                    if (hitsSinceLastMultiplier >= COMBO_THRESHOLD && multiplier < MAX_MULTIPLIER) {
                        multiplier++;
                        hitsSinceLastMultiplier = 0;
                    } else if (multiplier == MAX_MULTIPLIER) {
                        hitsSinceLastMultiplier = Math.min(hitsSinceLastMultiplier, COMBO_THRESHOLD); 
                    }
                    
                    score += (POINTS_PER_NOTE * multiplier); 
                    
                    setHitFeedback("ACERTOU!", Color.GREEN);

                    note.hit = true;
                    notes.removeIndex(i);
                    hitSomething = true;
                    break;
                }
            }
        }
        
        if (!hitSomething) {
            score = (score >= 2) ? score - 2 : 0;
            combo = 0;
            multiplier = 1;
            hitsSinceLastMultiplier = 0;
            setHitFeedback("ERROU!", Color.RED);
        }
    }

    // NOVO: Método para renderizar apenas visualmente (sem atualizar lógica)
    public void renderGameOnly() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();

        // 1. Cenário
        if (sceneryTexture != null) {
            float texWidth = sceneryTexture.getWidth();
            float texHeight = sceneryTexture.getHeight();

            // Mantém proporção e centraliza horizontalmente
            float scale = V_WIDTH / texWidth; // ajusta para caber na largura da tela
            float drawWidth = texWidth * scale;
            float drawHeight = texHeight * scale;
            float drawX = (V_WIDTH - drawWidth) / 2f; // centraliza o fundo

            // Faz o scroll mais lento (parallax)
            float currentY = -(sceneryScrollY % drawHeight);

            // Quantas cópias são necessárias para cobrir a altura
            int copies = (int)Math.ceil(V_HEIGHT / drawHeight) + 1;

            for (int i = 0; i < copies; i++) {
                batch.draw(sceneryTexture, drawX, currentY + i * drawHeight, drawWidth, drawHeight);
            }
        }
        
        // 2. Rua (Scroll)
        if (roadTexture != null && roadTexture.getHeight() > 0) {
            float roadVisualWidth = 240; 
            float roadVisualX = (V_WIDTH / 2f) - (roadVisualWidth / 2f);
            float currentY = -(roadScrollY % roadTexture.getHeight());
            batch.draw(roadTexture, roadVisualX, currentY, roadVisualWidth, roadTexture.getHeight());
            batch.draw(roadTexture, roadVisualX, currentY + roadTexture.getHeight(), roadVisualWidth, roadTexture.getHeight());
        }
        
        // 3. Moto
        if (motoTexture != null) {
            batch.draw(motoTexture, (V_WIDTH / 2f) - (motoTexture.getWidth() / 2f) , 10);
        }
        
        // 4. UI (Texto)
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, 20, (V_HEIGHT - 20));

        // UI de Combo/Multiplicador
        if (multiplier > 1) {
            font.setColor(Color.YELLOW);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, "x" + multiplier, 20, (V_HEIGHT - 45));

        if (combo > 0) {
            font.setColor(Color.WHITE);
            font.draw(batch, "" + combo, 20, (V_HEIGHT - 70)); 
        }

        // Feedback de Acerto/Erro
        if (feedbackTimer > 0 && !hitFeedback.isEmpty()) {
            font.setColor(hitFeedbackColor);
            font.draw(batch, hitFeedback, (V_WIDTH / 2f) - 35, 150);
        }
        
        batch.end();

        // ShapeRenderer (Notas, Hitboxes e Barra)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Zonas de Acerto
        shapeRenderer.setColor(0, 1, 0, 0.3f);
        shapeRenderer.rect(hitZoneLeft.x, hitZoneLeft.y, hitZoneLeft.width, hitZoneLeft.height);
        shapeRenderer.setColor(1, 0, 0, 0.3f);
        shapeRenderer.rect(hitZoneRight.x, hitZoneRight.y, hitZoneRight.width, hitZoneRight.height);
        
        // Notas
        for (Note note : notes) {
            if (note.hit) continue;
            shapeRenderer.setColor((note.type == Note.NoteType.LEFT) ? Color.CYAN : Color.MAGENTA);
            shapeRenderer.rect(note.rect.x, note.rect.y, note.rect.width, note.rect.height);
        }

        // Barra de progresso do multiplicador
        if (multiplier < MAX_MULTIPLIER) {
            float barX = 20;
            float barY = V_HEIGHT - 90;
            float barWidth = 80;
            float barHeight = 8;

            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            float progress = (float)hitsSinceLastMultiplier / COMBO_THRESHOLD;
            
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        }
        
        shapeRenderer.end();
    }

    @Override
    public void render(float delta) {
        // NOVO: Verifica se ESC foi pressionado para pausar
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (musica != null) {
                musica.pause(); // Pausa a música
            }
            game.setScreen(new PauseMenuScreen(game, this));
            return; // Não executa o resto do render
        }

        updateLogic(delta);
        renderGameOnly();
    }
    
    // Método para retomar a música quando voltar do pause
    public void resumeMusic() {
        if (musica != null && !musica.isPlaying()) {
            musica.play();
        }
    }

    @Override
    public void hide() {
        // Não dispose aqui porque pode estar pausado
    }

    @Override
    public void dispose() {
        if (musica != null) musica.dispose();
        if (motoTexture != null) motoTexture.dispose();
        if (roadTexture != null) roadTexture.dispose();
        if (sceneryTexture != null) sceneryTexture.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
}
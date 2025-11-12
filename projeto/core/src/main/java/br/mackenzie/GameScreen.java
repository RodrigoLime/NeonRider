package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    
    // --- Classes internas Note e Particle (Formatadas) ---
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
    private static class Particle {
        float x, y, velX, velY, life, maxLife;
        Color color;
        private static final float GRAVITY = -400f; 
        public Particle(float x, float y, float velX, float velY, float life, Color color) {
            this.x = x;
            this.y = y;
            this.velX = velX;
            this.velY = velY;
            this.life = life;
            this.maxLife = life;
            this.color = color.cpy();
        }
        public void update(float delta) {
            velY += GRAVITY * delta;
            x += velX * delta;
            y += velY * delta;
            life -= delta;
        }
        public boolean isDead() {
            return life <= 0;
        }
    }

    // --- Variáveis da Classe ---
    private final Main game;
    private final GameSettings settings;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;
    private Music musica;
    private Texture motoTexture, roadTexture, sceneryTexture;
    private float laneLeftX, laneRightX, laneWidth = 50;
    private Rectangle hitZoneLeft, hitZoneRight;
    private float hitZoneY = 100;
    private Array<Note> notes;
    private Array<Particle> particles; 
    private long lastNoteTime;
    private float noteSpeed = 300.0f;
    private long spawnInterval;
    private float roadScrollY = 0, sceneryScrollY = 0;
    private float scenerySpeedMultiplier = 0.2f; 
    private int score = 0, combo = 0, multiplier = 1;
    private final int COMBO_THRESHOLD = 10, MAX_MULTIPLIER = 4, POINTS_PER_NOTE = 10;
    private int hitsSinceLastMultiplier = 0; 
    private String hitFeedback = "";
    private float feedbackTimer = 0;
    private Color hitFeedbackColor = Color.WHITE;
    private int noteSpawnCount = 0;
    private final float V_WIDTH = SettingsScreen.GAME_WIDTH;
    private final float V_HEIGHT = SettingsScreen.GAME_HEIGHT;
    private GlyphLayout layout; 
    private long lastTapTime = 0;
    private final long DOUBLE_TAP_TIME = 250; 

    // --- Construtor ---
    public GameScreen(final Main game) {
        this.game = game;
        this.settings = game.settings;
        this.batch = game.batch;
        this.font = game.font;
        this.shapeRenderer = game.shapeRenderer;
        this.spawnInterval = settings.difficulty.spawnInterval;
        
        this.layout = new GlyphLayout(); 

        try {
            sceneryTexture = new Texture(Gdx.files.internal("background-1.png"));
            sceneryTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar background-1.png", e); }
        
        try {
            roadTexture = new Texture(Gdx.files.internal("neon_road.png"));
            roadTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) { Gdx.app.error("Texture", "Nao foi possivel carregar neon_road.png", e); }
        
        try {
            motoTexture = new Texture(Gdx.files.internal("neon_rider_idle.png"));
            motoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
        particles = new Array<>(); 
        lastNoteTime = TimeUtils.millis();
    }
    
    @Override
    public void show() {
        resumeMusic();
        Gdx.input.setInputProcessor(null);
    }

    public void pauseMusic() {
        if (musica != null) {
            musica.pause();
        }
    }

    public void updateMusicVolume() {
        if (musica != null) {
            musica.setVolume(game.settings.volume);
        }
    }

    public void resumeMusic() {
        if (musica != null) {
            updateMusicVolume();
            musica.play();
        }
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

    private void spawnParticles(float x, float y, Color color) {
        int particleCount = 20;
        for (int i = 0; i < particleCount; i++) {
            float velX = MathUtils.random(-150f, 150f);
            float velY = MathUtils.random(50f, 250f);
            float life = MathUtils.random(0.3f, 0.8f);
            particles.add(new Particle(x, y, velX, velY, life, color));
        }
    }
    
    private void updateLogic(float delta) {
        if (feedbackTimer > 0) {
            feedbackTimer -= delta;
            if (feedbackTimer <= 0) hitFeedback = "";
        }
        if (TimeUtils.millis() - lastNoteTime > spawnInterval) {
            spawnNote();
        }
        for (int i = notes.size - 1; i >= 0; i--) {
            Note note = notes.get(i);
            note.update(delta, noteSpeed);
            if (note.rect.y < hitZoneY - note.rect.height && !note.hit) {
                score = (score >= 5) ? score - 5 : 0;
                combo = 0;
                multiplier = 1;
                hitsSinceLastMultiplier = 0;
                setHitFeedback("PERDEU!", Color.GRAY);
                notes.removeIndex(i);
            }
        }
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) particles.removeIndex(i);
        }
        
        handleInput();
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseMusic();
            game.setScreen(new PauseMenuScreen(game, this));
        }
        
        roadScrollY += delta * noteSpeed;
        sceneryScrollY += delta * noteSpeed * scenerySpeedMultiplier;
    }

    // --- INÍCIO DA MUDANÇA ---
    private void toggleScreenMode() {
        // Usa o WindowService que está no Main (game)
        if (game.settings.screenMode == GameSettings.ScreenMode.WINDOWED) {
            game.settings.screenMode = GameSettings.ScreenMode.BORDERLESS;
            game.windowService.setBorderless();
            
        } else if (game.settings.screenMode == GameSettings.ScreenMode.BORDERLESS) {
            game.settings.screenMode = GameSettings.ScreenMode.WINDOWED;
            game.windowService.setWindowed();
        }
        // Não faz nada se estiver em FULLSCREEN
    }
    // --- FIM DA MUDANÇA ---

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(settings.keyLeft)) {
            checkHit(Note.NoteType.LEFT, hitZoneLeft);
        }
        if (Gdx.input.isKeyJustPressed(settings.keyRight)) {
            checkHit(Note.NoteType.RIGHT, hitZoneRight);
        }

        if (Gdx.input.justTouched()) {
            long currentTime = TimeUtils.millis();
            if (currentTime - lastTapTime < DOUBLE_TAP_TIME) {
                toggleScreenMode();
            }
            lastTapTime = currentTime;
        }
    }

    private void checkHit(Note.NoteType type, Rectangle hitZone) {
        boolean hitSomething = false;
        for (int i = notes.size - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (note.type == type && !note.hit) {
                if (note.rect.overlaps(hitZone)) {
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
                    Color particleColor = (note.type == Note.NoteType.LEFT) ? Color.CYAN : Color.MAGENTA;
                    float centerX = note.rect.x + note.rect.width / 2;
                    float centerY = note.rect.y + note.rect.height / 2;
                    spawnParticles(centerX, centerY, particleColor);
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

    @Override
    public void render(float delta) { 
        updateLogic(delta); 
        renderGameOnly(); 
    }

    public void renderGameOnly() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        
        if (sceneryTexture != null) {
            float texWidth = sceneryTexture.getWidth(), texHeight = sceneryTexture.getHeight();
            float scale = V_WIDTH / texWidth;
            float drawWidth = texWidth * scale, drawHeight = texHeight * scale;
            float drawX = (V_WIDTH - drawWidth) / 2f;
            float currentY = -(sceneryScrollY % drawHeight);
            int copies = (int)Math.ceil(V_HEIGHT / drawHeight) + 1;
            for (int i = 0; i < copies; i++) {
                batch.draw(sceneryTexture, drawX, currentY + i * drawHeight, drawWidth, drawHeight);
            }
        }
        if (roadTexture != null && roadTexture.getHeight() > 0) {
            float roadVisualWidth = 240;
            float roadVisualX = (V_WIDTH / 2f) - (roadVisualWidth / 2f);
            float currentY = -(roadScrollY % roadTexture.getHeight());
            batch.draw(roadTexture, roadVisualX, currentY, roadVisualWidth, roadTexture.getHeight());
            batch.draw(roadTexture, roadVisualX, currentY + roadTexture.getHeight(), roadVisualWidth, roadTexture.getHeight());
        }
        if (motoTexture != null) {
            batch.draw(motoTexture, (V_WIDTH / 2f) - (motoTexture.getWidth() / 2f) , 10);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, 20, (V_HEIGHT - 20));
        
        if (multiplier > 1) font.setColor(Color.YELLOW);
        else font.setColor(Color.WHITE);
        font.draw(batch, "x" + multiplier, 20, (V_HEIGHT - 45));
        
        if (combo > 0) {
            font.setColor(Color.WHITE);
            font.draw(batch, "" + combo, 20, (V_HEIGHT - 70));
        }

        layout.setText(font, "ESC para Pausar");
        font.draw(batch, layout, V_WIDTH - layout.width - 20f, V_HEIGHT - 20f); 

        if (feedbackTimer > 0 && !hitFeedback.isEmpty()) {
            font.setColor(hitFeedbackColor);
            layout.setText(font, hitFeedback);
            font.draw(batch, layout, (V_WIDTH - layout.width) / 2f, 150);
        }
        
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(0, 1, 0, 0.3f);
        shapeRenderer.rect(hitZoneLeft.x, hitZoneLeft.y, hitZoneLeft.width, hitZoneLeft.height);
        shapeRenderer.setColor(1, 0, 0, 0.3f);
        shapeRenderer.rect(hitZoneRight.x, hitZoneRight.y, hitZoneRight.width, hitZoneRight.height);
        
        for (Note note : notes) {
            if (note.hit) continue;
            shapeRenderer.setColor((note.type == Note.NoteType.LEFT) ? Color.CYAN : Color.MAGENTA);
            shapeRenderer.rect(note.rect.x, note.rect.y, note.rect.width, note.rect.height);
        }
        
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, alpha);
            shapeRenderer.rect(p.x - 1, p.y - 1, 3, 3);
        }
        
        if (multiplier < MAX_MULTIPLIER) {
            float barX = 20, barY = V_HEIGHT - 90, barWidth = 80, barHeight = 8;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            float progress = (float)hitsSinceLastMultiplier / COMBO_THRESHOLD;
            shapeRenderer.setColor(Color.LIME); 
            shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        }
        
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (musica != null) musica.dispose();
        if (motoTexture != null) motoTexture.dispose();
        if (roadTexture != null) roadTexture.dispose();
        if (sceneryTexture != null) sceneryTexture.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
    }
    
    @Override
    public void pause() {
        pauseMusic();
        game.setScreen(new PauseMenuScreen(game, this));
    }
    
    @Override
    public void resume() {
    }
}
package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class SettingsScreen extends InputAdapter implements Screen {

    private final Main game;
    private Vector3 touchPoint;
    private GlyphLayout layout;

    // Elementos do Slider
    private Rectangle volumeSliderTrack, volumeSliderKnob, volumeSliderTouchArea;
    private boolean isDraggingSlider = false;
    
    // Outros botões
    private Rectangle keyLeftButton, keyRightButton, backButton;
    private Rectangle windowedButton, borderlessButton, fullscreenButton;

    // Tamanho da tela virtual
    public static final int GAME_WIDTH = 854;  // 16:9
    public static final int GAME_HEIGHT = 480; // 16:9

    // Controle de origem
    private final boolean fromPauseMenu;
    private final PauseMenuScreen pauseMenuScreen;
    private final GameScreen gameScreen;

    // Estados para remapeamento
    private boolean waitingForKeyLeft = false;
    private boolean waitingForKeyRight = false;

    // Construtor normal
    public SettingsScreen(final Main game) {
        this(game, false, null, null);
    }

    // Construtor quando vem do pause
    public SettingsScreen(final Main game, boolean fromPauseMenu, PauseMenuScreen pauseMenuScreen, GameScreen gameScreen) {
        this.game = game;
        this.fromPauseMenu = fromPauseMenu;
        this.pauseMenuScreen = pauseMenuScreen;
        this.gameScreen = gameScreen;
        
        this.layout = new GlyphLayout();
        this.touchPoint = new Vector3();

        // Layout dos botões
        float smallWidth = 98, largeWidth = 300, spacing = 5, y = 400;

        float x = (GAME_WIDTH / 2f) - (smallWidth * 3 + spacing * 2) / 2f;
        windowedButton = new Rectangle(x, y, smallWidth, 40);
        borderlessButton = new Rectangle(x + smallWidth + spacing, y, smallWidth, 40);
        fullscreenButton = new Rectangle(x + smallWidth * 2 + spacing * 2, y, smallWidth, 40);
        
        y -= 90; 
        
        x = (GAME_WIDTH / 2f) - (largeWidth / 2f);
        volumeSliderTrack = new Rectangle(x, y, largeWidth, 10);
        volumeSliderKnob = new Rectangle(x, y - 7.5f, 15, 25);
        volumeSliderTouchArea = new Rectangle(x, y - 15, largeWidth, 40);

        y -= 100; 
        
        keyLeftButton = new Rectangle(x, y, largeWidth, 40);
        y -= 50;
        keyRightButton = new Rectangle(x, y, largeWidth, 40);

        y -= 100; 
        
        backButton = new Rectangle(x, y, largeWidth, 40);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // 1. Renderiza fundo
        if (fromPauseMenu && gameScreen != null) {
            gameScreen.renderGameOnly();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.setColor(0, 0, 0, 0.7f);
            game.shapeRenderer.rect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            game.shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } else {
            Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        // 2. Desenha os "botões" e o SLIDER
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        game.shapeRenderer.setColor(game.settings.screenMode == GameSettings.ScreenMode.WINDOWED ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(windowedButton.x, windowedButton.y, windowedButton.width, windowedButton.height);
        game.shapeRenderer.setColor(game.settings.screenMode == GameSettings.ScreenMode.BORDERLESS ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(borderlessButton.x, borderlessButton.y, borderlessButton.width, borderlessButton.height);
        game.shapeRenderer.setColor(game.settings.screenMode == GameSettings.ScreenMode.FULLSCREEN ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(fullscreenButton.x, fullscreenButton.y, fullscreenButton.width, fullscreenButton.height);
        
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(volumeSliderTrack.x, volumeSliderTrack.y, volumeSliderTrack.width, volumeSliderTrack.height);

        float knobX = MathUtils.clamp(
            volumeSliderTrack.x + (volumeSliderTrack.width * game.settings.volume) - (volumeSliderKnob.width / 2f),
            volumeSliderTrack.x,
            volumeSliderTrack.x + volumeSliderTrack.width - volumeSliderKnob.width
        );
        volumeSliderKnob.x = knobX;
        game.shapeRenderer.setColor(isDraggingSlider ? Color.GREEN : Color.WHITE);
        game.shapeRenderer.rect(volumeSliderKnob.x, volumeSliderKnob.y, volumeSliderKnob.width, volumeSliderKnob.height);
        
        game.shapeRenderer.setColor(waitingForKeyLeft ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(keyLeftButton.x, keyLeftButton.y, keyLeftButton.width, keyLeftButton.height);
        game.shapeRenderer.setColor(waitingForKeyRight ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(keyRightButton.x, keyRightButton.y, keyRightButton.width, keyRightButton.height);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        
        game.shapeRenderer.end();

        // 3. Desenha o texto (centralizado)
        game.batch.begin();
        game.font.setColor(Color.WHITE);

        layout.setText(game.font, "Janela");
        game.font.draw(game.batch, layout, windowedButton.x + (windowedButton.width - layout.width) / 2f, windowedButton.y + (windowedButton.height + layout.height) / 2f);
        layout.setText(game.font, "S/ Borda");
        game.font.draw(game.batch, layout, borderlessButton.x + (borderlessButton.width - layout.width) / 2f, borderlessButton.y + (borderlessButton.height + layout.height) / 2f);
        layout.setText(game.font, "Tela Cheia");
        game.font.draw(game.batch, layout, fullscreenButton.x + (fullscreenButton.width - layout.width) / 2f, fullscreenButton.y + (fullscreenButton.height + layout.height) / 2f);
        
        String volText = "Volume: " + (int)(game.settings.volume * 100);
        layout.setText(game.font, volText);
        game.font.draw(game.batch, layout, (GAME_WIDTH - layout.width) / 2f, volumeSliderTrack.y + 35);
        
        String leftKey = waitingForKeyLeft ? "Pressione..." : "Esquerda: " + Input.Keys.toString(game.settings.keyLeft);
        String rightKey = waitingForKeyRight ? "Pressione..." : "Direita: " + Input.Keys.toString(game.settings.keyRight);
        layout.setText(game.font, leftKey);
        game.font.draw(game.batch, layout, keyLeftButton.x + (keyLeftButton.width - layout.width) / 2f, keyLeftButton.y + (keyLeftButton.height + layout.height) / 2f);
        layout.setText(game.font, rightKey);
        game.font.draw(game.batch, layout, keyRightButton.x + (keyRightButton.width - layout.width) / 2f, keyRightButton.y + (keyRightButton.height + layout.height) / 2f);

        String backText = fromPauseMenu ? "Voltar ao Pause" : "Voltar ao Menu";
        layout.setText(game.font, backText);
        game.font.draw(game.batch, layout, backButton.x + (backButton.width - layout.width) / 2f, backButton.y + (backButton.height + layout.height) / 2f);
        
        game.batch.end();
    }

    private void updateVolumeFromTouch(float touchX) {
        float relativeX = touchX - volumeSliderTrack.x;
        float percent = relativeX / volumeSliderTrack.width;
        
        game.settings.volume = MathUtils.clamp(percent, 0f, 1f);

        if (gameScreen != null) {
            gameScreen.updateMusicVolume();
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchPoint.set(screenX, screenY, 0);
        game.viewport.unproject(touchPoint);
        
        waitingForKeyLeft = false;
        waitingForKeyRight = false;

        if (volumeSliderTouchArea.contains(touchPoint.x, touchPoint.y)) {
            isDraggingSlider = true;
            updateVolumeFromTouch(touchPoint.x);
            return true;
        }
        
        // --- INÍCIO DA MUDANÇA (Usando o WindowService) ---
        // Não precisamos mais atualizar o viewport manualmente aqui.
        // O Main.java vai receber o evento 'resize' e cuidar disso.
        
        if (windowedButton.contains(touchPoint.x, touchPoint.y)) {
            game.settings.screenMode = GameSettings.ScreenMode.WINDOWED;
            game.windowService.setWindowed(); // Chama o serviço
            
        } else if (borderlessButton.contains(touchPoint.x, touchPoint.y)) {
            game.settings.screenMode = GameSettings.ScreenMode.BORDERLESS;
            game.windowService.setBorderless(); // Chama o serviço
            
        } else if (fullscreenButton.contains(touchPoint.x, touchPoint.y)) {
            game.settings.screenMode = GameSettings.ScreenMode.FULLSCREEN;
            game.windowService.setFullscreen(); // Chama o serviço
        
        // --- FIM DA MUDANÇA ---
        
        } else if (keyLeftButton.contains(touchPoint.x, touchPoint.y)) {
            waitingForKeyLeft = true;
        } else if (keyRightButton.contains(touchPoint.x, touchPoint.y)) {
            waitingForKeyRight = true;
        
        } else if (backButton.contains(touchPoint.x, touchPoint.y)) {
            if (fromPauseMenu && pauseMenuScreen != null && gameScreen != null) {
                game.setScreen(new PauseMenuScreen(game, gameScreen));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            dispose();
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isDraggingSlider) {
            touchPoint.set(screenX, screenY, 0);
            game.viewport.unproject(touchPoint);
            updateVolumeFromTouch(touchPoint.x);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isDraggingSlider) {
            isDraggingSlider = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (waitingForKeyLeft) {
            game.settings.keyLeft = keycode;
            waitingForKeyLeft = false;
            return true;
        }
        if (waitingForKeyRight) {
            game.settings.keyRight = keycode;
            waitingForKeyRight = false;
            return true;
        }
        return false;
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }
}
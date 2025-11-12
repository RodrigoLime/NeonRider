package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class SettingsScreen extends InputAdapter implements Screen {

    private final Main game;
    private Rectangle volUpButton, volDownButton, keyLeftButton, keyRightButton, backButton;
    private Vector3 touchPoint;

    // Tamanho da tela virtual (constantes)
    public static final int GAME_WIDTH = 640;
    public static final int GAME_HEIGHT = 480;

    // Controle de origem (menu principal ou pause)
    private final boolean fromPauseMenu;
    private final PauseMenuScreen pauseMenuScreen;
    private final GameScreen gameScreen;

    // Estados para remapeamento
    private boolean waitingForKeyLeft = false;
    private boolean waitingForKeyRight = false;

    // Construtor normal (vindo do menu principal)
    public SettingsScreen(final Main game) {
        this(game, false, null, null);
    }

    // Construtor quando vem do pause
    public SettingsScreen(final Main game, boolean fromPauseMenu, PauseMenuScreen pauseMenuScreen, GameScreen gameScreen) {
        this.game = game;
        this.fromPauseMenu = fromPauseMenu;
        this.pauseMenuScreen = pauseMenuScreen;
        this.gameScreen = gameScreen;
        
        // Usamos coordenadas virtuais
        float x = GAME_WIDTH / 2 - 150;

        volUpButton = new Rectangle(x + 160, 300, 140, 40);
        volDownButton = new Rectangle(x, 300, 140, 40);
        keyLeftButton = new Rectangle(x, 200, 300, 40);
        keyRightButton = new Rectangle(x, 150, 300, 40);
        backButton = new Rectangle(x, 50, 300, 40);

        touchPoint = new Vector3();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // Se veio do pause, renderiza o jogo em baixo
        if (fromPauseMenu && gameScreen != null) {
            gameScreen.renderGameOnly();
            
            // Overlay escuro
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.setColor(0, 0, 0, 0.7f);
            game.shapeRenderer.rect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            game.shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        } else {
            // Fundo normal
            Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        // Desenha os "botões"
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Color.GRAY);
        game.shapeRenderer.rect(volUpButton.x, volUpButton.y, volUpButton.width, volUpButton.height);
        game.shapeRenderer.rect(volDownButton.x, volDownButton.y, volDownButton.width, volDownButton.height);
        game.shapeRenderer.setColor(waitingForKeyLeft ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(keyLeftButton.x, keyLeftButton.y, keyLeftButton.width, keyLeftButton.height);
        game.shapeRenderer.setColor(waitingForKeyRight ? Color.GREEN : Color.GRAY);
        game.shapeRenderer.rect(keyRightButton.x, keyRightButton.y, keyRightButton.width, keyRightButton.height);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        game.shapeRenderer.end();

        // Desenha o texto
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Volume: " + (int)(game.settings.volume * 100), keyLeftButton.x + 100, 380);
        game.font.draw(game.batch, "Diminuir (-)", volDownButton.x + 20, volDownButton.y + 25);
        game.font.draw(game.batch, "Aumentar (+)", volUpButton.x + 20, volUpButton.y + 25);

        String leftKey = waitingForKeyLeft ? "Pressione..." : Input.Keys.toString(game.settings.keyLeft);
        String rightKey = waitingForKeyRight ? "Pressione..." : Input.Keys.toString(game.settings.keyRight);
        game.font.draw(game.batch, "Tecla Esquerda: " + leftKey, keyLeftButton.x + 20, keyLeftButton.y + 25);
        game.font.draw(game.batch, "Tecla Direita: " + rightKey, keyRightButton.x + 20, keyRightButton.y + 25);

        String backText = fromPauseMenu ? "Voltar ao Pause" : "Voltar ao Menu";
        game.font.draw(game.batch, backText, backButton.x + 70, backButton.y + 25);
        game.batch.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchPoint.set(screenX, screenY, 0);
        game.viewport.unproject(touchPoint);
        
        waitingForKeyLeft = false;
        waitingForKeyRight = false;

        if (volUpButton.contains(touchPoint.x, touchPoint.y)) {
            game.settings.volume = Math.min(1.0f, game.settings.volume + 0.1f);
        } else if (volDownButton.contains(touchPoint.x, touchPoint.y)) {
            game.settings.volume = Math.max(0.0f, game.settings.volume - 0.1f);
        } else if (keyLeftButton.contains(touchPoint.x, touchPoint.y)) {
            waitingForKeyLeft = true;
        } else if (keyRightButton.contains(touchPoint.x, touchPoint.y)) {
            waitingForKeyRight = true;
        } else if (backButton.contains(touchPoint.x, touchPoint.y)) {
            // Volta para o lugar correto dependendo da origem
            if (fromPauseMenu && pauseMenuScreen != null && gameScreen != null) {
                game.setScreen(new PauseMenuScreen(game, gameScreen));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            dispose();
        }
        return true;
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

    @Override public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
}
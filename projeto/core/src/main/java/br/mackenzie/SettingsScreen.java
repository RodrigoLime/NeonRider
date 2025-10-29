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

    // Tamanho da tela
    public static final int GAME_WIDTH = 640;
    public static final int GAME_HEIGHT = 480;


    // Estados para remapeamento
    private boolean waitingForKeyLeft = false;
    private boolean waitingForKeyRight = false;

    public SettingsScreen(final Main game) {
        this.game = game;
        float x = Gdx.graphics.getWidth() / 2 - 150;

        volUpButton = new Rectangle(x + 160, 300, 140, 40);
        volDownButton = new Rectangle(x, 300, 140, 40);
        keyLeftButton = new Rectangle(x, 200, 300, 40);
        keyRightButton = new Rectangle(x, 150, 300, 40);
        backButton = new Rectangle(x, 50, 300, 40);

        touchPoint = new Vector3();
    }

    @Override
    public void show() {
        // Define esta classe como o processador de input para capturar teclas
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        // Volume
        game.font.draw(game.batch, "Volume: " + (int)(game.settings.volume * 100), keyLeftButton.x + 100, 380);
        game.font.draw(game.batch, "Diminuir (-)", volDownButton.x + 20, volDownButton.y + 25);
        game.font.draw(game.batch, "Aumentar (+)", volUpButton.x + 20, volUpButton.y + 25);

        // Teclas
        String leftKey = waitingForKeyLeft ? "Pressione..." : Input.Keys.toString(game.settings.keyLeft);
        String rightKey = waitingForKeyRight ? "Pressione..." : Input.Keys.toString(game.settings.keyRight);
        game.font.draw(game.batch, "Tecla Esquerda: " + leftKey, keyLeftButton.x + 20, keyLeftButton.y + 25);
        game.font.draw(game.batch, "Tecla Direita: " + rightKey, keyRightButton.x + 20, keyRightButton.y + 25);

        game.font.draw(game.batch, "Voltar ao Menu", backButton.x + 80, backButton.y + 25);
        game.batch.end();
    }

    // --- Métodos do InputAdapter ---

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        // --- LINHA CORRIGIDA ---
        // Converte coordenadas (origem no topo) para coordenadas do mundo (origem embaixo)
        touchPoint.set(screenX, Gdx.graphics.getHeight() - screenY, 0);
        // --- FIM DA CORREÇÃO ---

        // Cancela o "esperando tecla" se clicar em qualquer outro botão
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
            game.setScreen(new MainMenuScreen(game)); // Volta ao menu
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Se estávamos esperando a tecla esquerda
        if (waitingForKeyLeft) {
            game.settings.keyLeft = keycode;
            waitingForKeyLeft = false;
            return true;
        }
        // Se estávamos esperando a tecla direita
        if (waitingForKeyRight) {
            game.settings.keyRight = keycode;
            waitingForKeyRight = false;
            return true;
        }
        return false;
    }

    // --- Métodos obrigatórios da interface Screen ---
    @Override public void hide() {
        // Limpa o processador de input ao sair da tela
        Gdx.input.setInputProcessor(null);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
}

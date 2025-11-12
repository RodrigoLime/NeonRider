package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import static br.mackenzie.SettingsScreen.GAME_WIDTH;
import static br.mackenzie.SettingsScreen.GAME_HEIGHT;

public class PauseMenuScreen implements Screen {
    private final Main game;
    private final GameScreen gameScreen; // Referência para o jogo pausado
    private int ignoreInputFrames = 3;

    private Rectangle continueButton;
    private Rectangle restartButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;

    private Vector3 touchPoint;

    public PauseMenuScreen(final Main game, final GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;

        float buttonWidth = 200;
        float buttonHeight = 50;
        float x = (GAME_WIDTH / 2f) - (buttonWidth / 2f);

        // Layout do menu de pausa
        continueButton = new Rectangle(x, 320, buttonWidth, buttonHeight);
        restartButton = new Rectangle(x, 250, buttonWidth, buttonHeight);
        settingsButton = new Rectangle(x, 180, buttonWidth, buttonHeight);
        exitButton = new Rectangle(x, 110, buttonWidth, buttonHeight);

        touchPoint = new Vector3();
    }

    @Override
    public void render(float delta) {
        // Renderiza o jogo congelado em baixo
        if (gameScreen != null) {
            gameScreen.renderGameOnly();
        }

        // Overlay escuro semi-transparente
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.7f);
        game.shapeRenderer.rect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        game.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Desenha os botões
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        game.shapeRenderer.setColor(Color.GREEN);
        game.shapeRenderer.rect(continueButton.x, continueButton.y, continueButton.width, continueButton.height);
        
        game.shapeRenderer.setColor(Color.YELLOW);
        game.shapeRenderer.rect(restartButton.x, restartButton.y, restartButton.width, restartButton.height);
        
        game.shapeRenderer.setColor(Color.GRAY);
        game.shapeRenderer.rect(settingsButton.x, settingsButton.y, settingsButton.width, settingsButton.height);
        
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        
        game.shapeRenderer.end();

        // Desenha os textos
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        
        // Título
        game.font.draw(game.batch, "PAUSE", GAME_WIDTH / 2f - 30, 420);
        
        // Textos dos botões
        game.font.draw(game.batch, "Continuar", continueButton.x + 65, continueButton.y + 30);
        game.font.draw(game.batch, "Reiniciar", restartButton.x + 65, restartButton.y + 30);
        game.font.draw(game.batch, "Configuracoes", settingsButton.x + 50, settingsButton.y + 30);
        game.font.draw(game.batch, "Sair para Menu", exitButton.x + 40, exitButton.y + 30);
        
        game.batch.end();

        // Ignora inputs iniciais
        if (ignoreInputFrames > 0) {
            ignoreInputFrames--;
            return;
        }

        // Processa inputs
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            if (continueButton.contains(touchPoint.x, touchPoint.y)) {
                // Volta para o jogo e retoma a música
                gameScreen.resumeMusic();
                game.setScreen(gameScreen);
                dispose();
                
            } else if (restartButton.contains(touchPoint.x, touchPoint.y)) {
                // Reinicia o jogo com a mesma dificuldade
                game.setScreen(new GameScreen(game));
                gameScreen.dispose();
                dispose();
                
            } else if (settingsButton.contains(touchPoint.x, touchPoint.y)) {
                // Vai para configurações (passando referência deste pause menu)
                game.setScreen(new SettingsScreen(game, true, this, gameScreen));
                dispose();
                
            } else if (exitButton.contains(touchPoint.x, touchPoint.y)) {
                // Volta para o menu principal
                game.setScreen(new MainMenuScreen(game));
                gameScreen.dispose();
                dispose();
            }
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
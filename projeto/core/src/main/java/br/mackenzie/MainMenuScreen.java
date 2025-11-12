package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import static br.mackenzie.SettingsScreen.GAME_WIDTH;

public class MainMenuScreen implements Screen {
    private final Main game;
    private int ignoreInputFrames = 3;

    private Rectangle playEasyButton;
    private Rectangle playMediumButton;
    private Rectangle playHardButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;

    private Vector3 touchPoint;

    public MainMenuScreen(final Main game) {
        this.game = game;

        float buttonWidth = 200;
        float buttonHeight = 50;
        float x = (GAME_WIDTH / 2f) - (buttonWidth / 2f);

        // Define posições Y em unidades virtuais
        playEasyButton = new Rectangle(x, 350, buttonWidth, buttonHeight);
        playMediumButton = new Rectangle(x, 280, buttonWidth, buttonHeight);
        playHardButton = new Rectangle(x, 210, buttonWidth, buttonHeight);
        settingsButton = new Rectangle(x, 140, buttonWidth, buttonHeight);
        exitButton = new Rectangle(x, 70, buttonWidth, buttonHeight);

        touchPoint = new Vector3();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Color.BLUE);
        game.shapeRenderer.rect(playEasyButton.x, playEasyButton.y, playEasyButton.width, playEasyButton.height);
        game.shapeRenderer.setColor(Color.ORANGE);
        game.shapeRenderer.rect(playMediumButton.x, playMediumButton.y, playMediumButton.width, playMediumButton.height);
        game.shapeRenderer.setColor(Color.RED);
        game.shapeRenderer.rect(playHardButton.x, playHardButton.y, playHardButton.width, playHardButton.height);
        game.shapeRenderer.setColor(Color.GRAY);
        game.shapeRenderer.rect(settingsButton.x, settingsButton.y, settingsButton.width, settingsButton.height);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        game.shapeRenderer.end();

        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Jogar (Facil)", playEasyButton.x + 60, playEasyButton.y + 30);
        game.font.draw(game.batch, "Jogar (Medio)", playMediumButton.x + 55, playMediumButton.y + 30);
        game.font.draw(game.batch, "Jogar (Dificil)", playHardButton.x + 50, playHardButton.y + 30);
        game.font.draw(game.batch, "Configuracoes", settingsButton.x + 50, settingsButton.y + 30);
        game.font.draw(game.batch, "Sair", exitButton.x + 85, exitButton.y + 30);
        game.batch.end();

        if (ignoreInputFrames > 0) {
            ignoreInputFrames--;
            return;
        }

        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            if (playEasyButton.contains(touchPoint.x, touchPoint.y)) {
                game.settings.difficulty = GameSettings.Difficulty.EASY;
                game.setScreen(new GameScreen(game));
                dispose();
            } else if (playMediumButton.contains(touchPoint.x, touchPoint.y)) {
                game.settings.difficulty = GameSettings.Difficulty.MEDIUM;
                game.setScreen(new GameScreen(game));
                dispose();
            } else if (playHardButton.contains(touchPoint.x, touchPoint.y)) {
                game.settings.difficulty = GameSettings.Difficulty.HARD;
                game.setScreen(new GameScreen(game));
                dispose();
            } else if (settingsButton.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new SettingsScreen(game));
                dispose();
            } else if (exitButton.contains(touchPoint.x, touchPoint.y)) {
                Gdx.app.exit();
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
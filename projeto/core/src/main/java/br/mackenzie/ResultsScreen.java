package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import static br.mackenzie.SettingsScreen.GAME_HEIGHT;
import static br.mackenzie.SettingsScreen.GAME_WIDTH;

public class ResultsScreen implements Screen {

    private final Main game;
    private final int finalScore;
    private final GameSettings.Difficulty difficulty;

    private Rectangle backButton;
    private Vector3 touchPoint;
    private GlyphLayout layout;

    public ResultsScreen(final Main game, int finalScore, GameSettings.Difficulty difficulty) {
        this.game = game;
        this.finalScore = finalScore;
        this.difficulty = difficulty;

        this.layout = new GlyphLayout();
        this.touchPoint = new Vector3();

        float buttonWidth = 200;
        float buttonHeight = 50;
        float x = (GAME_WIDTH / 2f) - (buttonWidth / 2f);
        
        backButton = new Rectangle(x, 100, buttonWidth, buttonHeight);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Desenha o botão
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Color.GRAY);
        game.shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        game.shapeRenderer.end();

        // Desenha os textos
        game.batch.begin();
        game.font.setColor(Color.WHITE);

        // Título
        layout.setText(game.font, "Fim de Jogo!");
        game.font.draw(game.batch, layout, (GAME_WIDTH - layout.width) / 2f, 400);

        // Dificuldade
        // (Usando .name() para converter o enum em String)
        layout.setText(game.font, "Dificuldade: " + difficulty.name());
        game.font.draw(game.batch, layout, (GAME_WIDTH - layout.width) / 2f, 350);

        // Pontuação
        layout.setText(game.font, "Pontuacao Final: " + finalScore);
        game.font.draw(game.batch, layout, (GAME_WIDTH - layout.width) / 2f, 300);

        // Texto do Botão
        layout.setText(game.font, "Voltar ao Menu");
        game.font.draw(game.batch, layout,
                backButton.x + (backButton.width - layout.width) / 2f,
                backButton.y + (backButton.height + layout.height) / 2f);

        game.batch.end();

        // Lógica de input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPoint);

            if (backButton.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }

    @Override public void show() {
        // Garante que o input processor seja esta tela
        Gdx.input.setInputProcessor(null);
    }
    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() {
        // Não há nada para "disposar" aqui, mas é uma boa prática
    }
}

package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Main extends Game {

    // Assets globais que todas as telas usarão
    public SpriteBatch batch;
    public BitmapFont font;
    public ShapeRenderer shapeRenderer;

    // Objeto global de configurações
    public GameSettings settings;

    @Override
    public void create() {
        // Inicializa os assets globais
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        // Inicializa as configurações
        settings = new GameSettings();

        // Define a primeira tela a ser mostrada (o Menu)
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    // Libera os assets globais
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}

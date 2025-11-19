package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {

    // Assets globais
    public SpriteBatch batch;
    public BitmapFont font;
    public ShapeRenderer shapeRenderer;

    // Configurações globais
    public GameSettings settings;

    // Viewport global
    public OrthographicCamera camera;
    public Viewport viewport;

    // Serviço de janela (injetado pelo Launcher)
    public final WindowService windowService;

    /**
     * Construtor modificado para aceitar o serviço da plataforma.
     */
    public Main(WindowService windowService) {
        this.windowService = windowService;
    }

    @Override
    public void create() {
        // Inicializa os assets globais
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Carregamento da Fonte
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/modern_sans_serif_7.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 64;
        parameter.color = Color.WHITE;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        font = generator.generateFont(parameter);

        float targetSize = 22f;
        font.getData().setScale(targetSize / parameter.size);

        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        generator.dispose();
        // --- Fim do Carregamento da Fonte ---

        settings = new GameSettings();

        camera = new OrthographicCamera();
        viewport = new FitViewport(SettingsScreen.GAME_WIDTH, SettingsScreen.GAME_HEIGHT, camera);
        viewport.apply();
        camera.position.set(SettingsScreen.GAME_WIDTH / 2f, SettingsScreen.GAME_HEIGHT / 2f, 0);
        camera.update();

        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Este método é chamado automaticamente pelo LibGDX
        // sempre que a janela muda de tamanho, corrigindo o "piscar"
        viewport.update(width, height, true);
    }
}

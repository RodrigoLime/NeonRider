package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {

    // Assets globais que todas as telas usarão
    public SpriteBatch batch;
    public BitmapFont font;
    public ShapeRenderer shapeRenderer;

    // Objeto global de configurações
    public GameSettings settings;

    // Câmera e Viewport para lidar com reescalonamento
    public OrthographicCamera camera;
    public Viewport viewport;

    @Override
    public void create() {
        // Inicializa os assets globais
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        // Inicializa as configurações
        settings = new GameSettings();

        // Configura a câmera e o viewport
        camera = new OrthographicCamera();
        // Usamos as constantes da SettingsScreen para definir o tamanho do mundo virtual
        viewport = new FitViewport(SettingsScreen.GAME_WIDTH, SettingsScreen.GAME_HEIGHT, camera);
        viewport.apply();
        // Centraliza a câmera no mundo virtual
        camera.position.set(SettingsScreen.GAME_WIDTH / 2f, SettingsScreen.GAME_HEIGHT / 2f, 0);
        camera.update(); // Atualiza a câmera imediatamente

        // Define a primeira tela a ser mostrada (o Menu)
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // --- CORREÇÃO AQUI ---
        // 1. Limpa a tela inteira com preto. Isso desenha as "barras pretas" (letterbox)
        //    do FitViewport se a proporção da tela for diferente de 640x480.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Aplica o viewport. Isso chama Gdx.gl.glViewport(...) internamente,
        //    restringindo o desenho à área calculada pelo viewport.
        //    ISSO É O QUE FALTAVA.
        viewport.apply();
        // --- FIM DA CORREÇÃO ---

        // 3. Atualiza a câmera
        camera.update();
        
        // 4. Aplica a projeção da câmera aos renderizadores
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 5. Renderiza a tela ativa.
        //    A tela ativa (ex: MainMenuScreen) irá então limpar *apenas* a área do viewport
        //    com sua própria cor de fundo, desenhando por cima das barras pretas.
        super.render();
    }

    // Libera os assets globais
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }

    // Atualiza o viewport quando a tela é redimensionada
    @Override
    public void resize(int width, int height) {
        // Atualiza o viewport com o novo tamanho da janela
        viewport.update(width, height);
        // Re-centraliza a câmera (importante para FitViewport)
        camera.position.set(SettingsScreen.GAME_WIDTH / 2f, SettingsScreen.GAME_HEIGHT / 2f, 0);
    }
}
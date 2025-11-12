package br.mackenzie.lwjgl3;

import br.mackenzie.SettingsScreen;
import br.mackenzie.WindowService;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;

// Importa a classe GLFW para controle de baixo nível da janela
import org.lwjgl.glfw.GLFW;

/**
 * Esta é a implementação "Desktop (LWJGL3)" do nosso contrato WindowService.
 * Ela usa o workaround GLFW (height + 1) para um modo "sem borda" estável.
 */
public class Lwjgl3WindowService implements WindowService {

    @Override
    public void setWindowed() {
        // 1. Adiciona as bordas (decoração)
        ((Lwjgl3Graphics) Gdx.graphics).setUndecorated(false); 
        
        // 2. Define o modo janela para o tamanho 16:9
        Gdx.graphics.setWindowedMode(
                SettingsScreen.GAME_WIDTH,
                SettingsScreen.GAME_HEIGHT
        );
    }

    @Override
    public void setBorderless() {
        // --- INÍCIO DA IMPLEMENTAÇÃO DO WORKAROUND ---
        
        // 1. Pega os objetos gráficos do LWJGL 3
        Lwjgl3Graphics g = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Monitor monitor = (Lwjgl3Monitor) g.getMonitor();
        Lwjgl3DisplayMode mode = (Lwjgl3DisplayMode) g.getDisplayMode(monitor);
        Lwjgl3Window window = g.getWindow();

        // 2. Garante que as bordas estão removidas
        g.setUndecorated(true);

        // 3. Define a posição da janela usando GLFW
        // (monitor.virtualX/Y é 0,0 para o monitor principal)
        GLFW.glfwSetWindowPos(
                window.getWindowHandle(),
                monitor.virtualX,
                monitor.virtualY
        );
        
        // 4. Define o tamanho da janela usando GLFW, com o truque do "+1"
        GLFW.glfwSetWindowSize(
                window.getWindowHandle(),
                mode.width,
                mode.height + 1 // O workaround que você encontrou
        );
        
        // --- FIM DA IMPLEMENTAÇÃO ---
    }

    @Override
    public void setFullscreen() {
        // O modo tela cheia exclusivo cuida de tudo
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }
}
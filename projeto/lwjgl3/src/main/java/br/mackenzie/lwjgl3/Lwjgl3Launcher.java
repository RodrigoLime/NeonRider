package br.mackenzie.lwjgl3;

import br.mackenzie.Main;
import br.mackenzie.SettingsScreen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Lwjgl3Launcher {
	public static void main(String[] args) {
		createApplication();
	}

	private static Lwjgl3Application createApplication() {
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("NeonRider");
		configuration.useVsync(true);

		configuration.setWindowedMode(SettingsScreen.GAME_WIDTH, SettingsScreen.GAME_HEIGHT);

		// --- INÍCIO DA MUDANÇA ---
		// 1. Cria a implementação do nosso serviço
		Lwjgl3WindowService windowService = new Lwjgl3WindowService();

		// 2. Injeta o serviço no construtor do Main
		return new Lwjgl3Application(new Main(windowService), configuration);
		// --- FIM DA MUDANÇA ---
	}
}
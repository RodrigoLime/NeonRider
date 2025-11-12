package br.mackenzie;

/**
 * Esta interface define um "contrato" para o módulo 'core'.
 * O 'core' não sabe como mudar a janela, mas sabe que precisa
 * de um serviço que possa fazer isso.
 */
public interface WindowService {

    /**
     * Define o jogo para o modo janela padrão (854x480 com bordas).
     */
    void setWindowed();

    /**
     * Define o jogo para o modo "Tela Cheia em Janela" (sem bordas).
     */
    void setBorderless();

    /**
     * Define o jogo para o modo "Tela Cheia Exclusiva".
     */
    void setFullscreen();
}
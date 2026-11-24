package br.com.monitorhack.config;

import org.openqa.selenium.WebDriver;

/**
 * Contrato simples que expõe o WebDriver de uma classe de teste.
 *
 * Usado pela {@code ScreenshotOnFailureExtension}: quando um teste falha, a
 * extensão pega a instância do teste, verifica se ela implementa esta interface
 * e, em caso positivo, obtém o driver para capturar o print da tela.
 */
public interface HasDriver {
    WebDriver getDriver();
}

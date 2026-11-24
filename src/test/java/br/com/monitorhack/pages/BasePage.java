package br.com.monitorhack.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Classe base dos Page Objects (padrão Page Object Model).
 *
 * O POM separa "o que a página faz" (métodos aqui e nas subclasses) de "como o
 * teste verifica" (asserts nas classes de teste). Se o HTML mudar, só mexemos
 * nos Page Objects — os testes continuam iguais.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    /**
     * Pausa (em milissegundos) aplicada após cada ação na tela — serve APENAS
     * para deixar a apresentação mais lenta e visível no data-show.
     * Controlada por -DdemoPauseMs=800. Padrão 0 = sem pausa (execução rápida).
     * Não faz parte da lógica do teste; é só recurso de demonstração.
     */
    private static final long DEMO_PAUSE_MS = Long.getLong("demoPauseMs", 0L);

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Espera explícita até um atributo do elemento atingir o valor esperado. */
    protected void waitAttribute(By locator, String attr, String expected) {
        wait.until(ExpectedConditions.attributeToBe(locator, attr, expected));
    }

    /** Espera explícita até o texto de um elemento ser exatamente o esperado. */
    protected void waitText(By locator, String expected) {
        wait.until(ExpectedConditions.textToBe(locator, expected));
    }

    /** Pausa visual para a apresentação (só atua se -DdemoPauseMs for informado). */
    protected void pause() {
        if (DEMO_PAUSE_MS > 0) {
            try {
                Thread.sleep(DEMO_PAUSE_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

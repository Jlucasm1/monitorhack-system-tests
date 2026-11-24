package br.com.monitorhack.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centraliza a criação do WebDriver.
 *
 * Usa o Google Chrome. A partir do Selenium 4.6, o "Selenium Manager" baixa e
 * configura o ChromeDriver automaticamente — por isso NÃO precisamos apontar
 * caminho de driver nem usar WebDriverManager.
 *
 * O modo headless (navegador invisível) é controlado pela propriedade de
 * sistema -Dheadless=true. Por padrão abre a janela do navegador, o que deixa
 * a apresentação no data-show bem mais visual.
 */
public final class WebDriverFactory {

    static {
        // Silencia os avisos de CDP do Selenium (ex.: quando o Chrome é mais novo
        // que o Selenium). Não afetam os testes; apenas limpam o console na demo.
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
    }

    private WebDriverFactory() { }

    public static WebDriver create() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1440,900");
        } else {
            options.addArguments("--start-maximized");
        }

        WebDriver driver = new ChromeDriver(options);
        // Espera implícita curta como "rede de segurança"; o grosso das esperas
        // é feito com WebDriverWait (espera explícita) nos Page Objects.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        return driver;
    }
}

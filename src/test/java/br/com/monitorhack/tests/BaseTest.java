package br.com.monitorhack.tests;

import br.com.monitorhack.config.HasDriver;
import br.com.monitorhack.config.LocalAppServer;
import br.com.monitorhack.config.WebDriverFactory;
import br.com.monitorhack.extensions.ScreenshotOnFailureExtension;
import br.com.monitorhack.pages.DashboardPage;
import br.com.monitorhack.pages.LoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * Classe base de todos os testes.
 *
 * Ciclo de vida (JUnit 5):
 *  @BeforeAll  (1x por classe) -> sobe o servidor HTTP local com o app
 *  @BeforeEach (antes de cada teste) -> cria um navegador NOVO (estado limpo)
 *  @AfterEach  (depois de cada teste) -> fecha o navegador
 *  @AfterAll   (1x por classe) -> derruba o servidor
 *
 * Cada teste roda em um navegador novo e recarrega a página, garantindo
 * ISOLAMENTO: um teste nunca interfere no outro.
 *
 * Implementa HasDriver para que a ScreenshotOnFailureExtension consiga o driver
 * em caso de falha.
 */
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseTest implements HasDriver {

    protected static final String USER = "analista";
    protected static final String PASS = "monitor2026";

    /** Pausa extra no fim de cada teste (modo apresentação), para a plateia
     *  conseguir ver o estado final da tela antes do navegador fechar. */
    private static final long DEMO_PAUSE_MS = Long.getLong("demoPauseMs", 0L);

    private static LocalAppServer server;
    protected static String baseUrl;

    protected WebDriver driver;
    protected LoginPage loginPage;

    @BeforeAll
    static void startServer() throws Exception {
        server = new LocalAppServer();
        server.start();
        baseUrl = server.getBaseUrl();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.create();
        loginPage = new LoginPage(driver).open(baseUrl);
    }

    @AfterEach
    void tearDown() {
        // No modo apresentação, segura a tela um instante antes de fechar.
        if (DEMO_PAUSE_MS > 0) {
            try {
                Thread.sleep(DEMO_PAUSE_MS * 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (driver != null) {
            driver.quit();
        }
    }

    /** Atalho: faz login com as credenciais válidas e devolve o dashboard pronto. */
    protected DashboardPage loginValido() {
        DashboardPage dash = loginPage.loginAs(USER, PASS);
        // garante que o painel carregou antes de qualquer ação
        org.junit.jupiter.api.Assertions.assertTrue(dash.isLoaded(), "O dashboard deveria estar carregado");
        return dash;
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }
}

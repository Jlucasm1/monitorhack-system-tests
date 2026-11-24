package br.com.monitorhack.tests;

import br.com.monitorhack.pages.DashboardPage;
import br.com.monitorhack.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de sistema da autenticação (controle de acesso ao SOC).
 */
@DisplayName("Autenticação no MonitorHack")
class LoginTest extends BaseTest {

    @Test
    @DisplayName("Login com credenciais válidas acessa o painel")
    void loginValidoAcessaPainel() {
        DashboardPage dash = loginPage.loginAs(USER, PASS);
        assertTrue(dash.isLoaded(), "O painel deveria abrir após login válido");
    }

    @Test
    @DisplayName("Login inválido exibe mensagem de erro e não entra")
    void loginInvalidoExibeErro() {
        LoginPage page = loginPage.loginExpectingFailure("analista", "senhaErrada");
        assertTrue(page.isErrorVisible(), "Deveria mostrar mensagem de erro");
        assertTrue(page.getErrorText().toLowerCase().contains("inválidas"),
                "A mensagem deveria indicar credenciais inválidas");
    }

    /**
     * Teste PARAMETRIZADO (data-driven): várias combinações inválidas,
     * um método só, cada linha vira um teste independente.
     */
    @ParameterizedTest(name = "[{index}] usuário=\"{0}\" / senha=\"{1}\" deve ser rejeitado")
    @DisplayName("Credenciais inválidas são sempre rejeitadas")
    @CsvSource({
            "analista,    1234",
            "root,        monitor2026",
            "analista,    MONITOR2026",
            "convidado,   convidado",
            "'',          ''"
    })
    void credenciaisInvalidasSaoRejeitadas(String user, String pass) {
        LoginPage page = loginPage.loginExpectingFailure(user, pass);
        assertTrue(page.isErrorVisible(),
                "A combinação inválida deveria ser rejeitada");
    }
}

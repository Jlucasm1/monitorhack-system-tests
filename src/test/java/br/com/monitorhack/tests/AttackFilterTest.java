package br.com.monitorhack.tests;

import br.com.monitorhack.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes DATA-DRIVEN (dirigidos por dados) dos filtros do painel.
 *
 * Em vez de escrever um método para cada tipo de ataque, usamos
 * @ParameterizedTest + @CsvSource: cada linha do CSV vira um caso de teste
 * com a contagem esperada de linhas visíveis. Isso mostra como cobrir muitos
 * cenários com pouco código.
 */
@DisplayName("Painel: filtros de eventos")
class AttackFilterTest extends BaseTest {

    @ParameterizedTest(name = "Filtrar tipo \"{0}\" deve exibir {1} evento(s)")
    @DisplayName("Filtro por tipo de ataque")
    @CsvSource({
            "Força Bruta,   3",
            "SQL Injection, 2",
            "Port Scan,     2",
            "DDoS,          2",
            "XSS,           2",
            "Malware,       1"
    })
    void filtroPorTipo(String tipo, int esperado) {
        DashboardPage dash = loginValido();
        dash.filterByType(tipo);
        assertEquals(esperado, dash.getVisibleRowCount(),
                "quantidade de linhas para o tipo " + tipo);
    }

    @ParameterizedTest(name = "Filtrar severidade \"{0}\" deve exibir {1} evento(s)")
    @DisplayName("Filtro por severidade")
    @CsvSource({
            "Crítico, 4",
            "Alto,    3",
            "Médio,   2",
            "Baixo,   3"
    })
    void filtroPorSeveridade(String severidade, int esperado) {
        DashboardPage dash = loginValido();
        dash.filterBySeverity(severidade);
        assertEquals(esperado, dash.getVisibleRowCount(),
                "quantidade de linhas para a severidade " + severidade);
    }

    @Test
    @DisplayName("Busca por prefixo de IP filtra corretamente")
    void buscaPorIp() {
        DashboardPage dash = loginValido();
        dash.searchByIp("198.51.100");
        // 4 eventos têm IP começando com 198.51.100 (linhas 3, 6, 8 e 12 do seed)
        assertEquals(4, dash.getVisibleRowCount(), "IPs da faixa 198.51.100.x");
    }

    @Test
    @DisplayName("Combinar tipo + severidade restringe o resultado")
    void filtroCombinado() {
        DashboardPage dash = loginValido();
        dash.filterByType("Força Bruta").filterBySeverity("Crítico");
        // Apenas 1 Força Bruta é Crítico (IP 203.0.113.10)
        assertEquals(1, dash.getVisibleRowCount(), "Força Bruta + Crítico");
    }

    @Test
    @DisplayName("Limpar filtros restaura os 12 eventos")
    void limparFiltros() {
        DashboardPage dash = loginValido();
        dash.filterByType("DDoS");
        assertEquals(2, dash.getVisibleRowCount(), "após filtrar DDoS");

        dash.clearFilters();
        assertEquals(12, dash.getVisibleRowCount(), "após limpar filtros");
    }
}

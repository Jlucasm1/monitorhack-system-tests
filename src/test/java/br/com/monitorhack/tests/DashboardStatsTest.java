package br.com.monitorhack.tests;

import br.com.monitorhack.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes das estatísticas e das ações do analista (bloquear / reconhecer).
 * Usa @Nested para agrupar cenários relacionados de forma legível no relatório.
 */
@DisplayName("Painel: estatísticas e ações do analista")
class DashboardStatsTest extends BaseTest {

    @Nested
    @DisplayName("Estado inicial (dados seed)")
    class EstadoInicial {

        @Test
        @DisplayName("Mostra 12 eventos, 4 críticos, 12 ativos e 0 bloqueados")
        void estatisticasIniciais() {
            DashboardPage dash = loginValido();
            assertEquals(12, dash.getTotal(),    "total de eventos");
            assertEquals(4,  dash.getCritical(), "eventos críticos");
            assertEquals(12, dash.getActive(),   "eventos ativos");
            assertEquals(0,  dash.getBlocked(),  "IPs bloqueados");
        }
    }

    @Nested
    @DisplayName("Ações sobre um evento")
    class Acoes {

        @Test
        @DisplayName("Bloquear um IP aumenta 'bloqueados' e reduz 'ativos'")
        void bloquearIpAtualizaEstatisticas() {
            DashboardPage dash = loginValido();

            dash.blockIp("203.0.113.10");

            assertEquals("bloqueado", dash.getStatusForIp("203.0.113.10"), "status da linha");
            assertEquals(1,  dash.getBlocked(), "IPs bloqueados após ação");
            assertEquals(11, dash.getActive(),  "ativos após bloquear 1");
            assertEquals(12, dash.getTotal(),   "total não muda ao bloquear");
        }

        @Test
        @DisplayName("Reconhecer um evento reduz 'ativos' sem bloquear")
        void reconhecerEventoReduzAtivos() {
            DashboardPage dash = loginValido();

            dash.acknowledgeIp("203.0.113.11");

            assertEquals("reconhecido", dash.getStatusForIp("203.0.113.11"), "status da linha");
            assertEquals(11, dash.getActive(),  "ativos após reconhecer 1");
            assertEquals(0,  dash.getBlocked(), "reconhecer não conta como bloqueado");
        }

        @Test
        @DisplayName("Simular ataque adiciona um novo evento visível")
        void simularAtaqueAdicionaEvento() {
            DashboardPage dash = loginValido();

            dash.simulateAttack();

            assertEquals(13, dash.getTotal(), "total após simular 1 ataque");
            org.junit.jupiter.api.Assertions.assertTrue(
                    dash.isRowVisibleForIp("203.0.113.99"),
                    "o novo ataque simulado deveria aparecer na tabela");
        }
    }
}

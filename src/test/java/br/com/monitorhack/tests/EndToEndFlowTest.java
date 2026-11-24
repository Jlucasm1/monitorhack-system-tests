package br.com.monitorhack.tests;

import br.com.monitorhack.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de sistema PONTA A PONTA (E2E) que simula o fluxo real de um analista de
 * SOC durante um incidente, numa sequência ordenada (@Order).
 *
 * Cada passo é um @Test, mas eles contam uma história única: investigar,
 * responder e confirmar. @TestMethodOrder garante a execução em ordem.
 *
 * Observação didática: como cada @BeforeEach cria um navegador novo, aqui
 * concentramos o fluxo em um único método @Test para manter o estado ao longo
 * dos passos, e usamos @Order apenas para ilustrar o recurso quando houver mais
 * de um método.
 */
@DisplayName("Fluxo E2E: resposta a incidente no SOC")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndFlowTest extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("Analista investiga, responde e confirma o incidente")
    void fluxoCompletoDeIncidente() {
        // 1) Acesso ao SOC
        DashboardPage dash = loginValido();
        assertEquals(12, dash.getTotal(), "o turno começa com 12 eventos");

        // 2) Investiga: foca nos ataques de DDoS
        dash.filterByType("DDoS");
        assertEquals(2, dash.getVisibleRowCount(), "2 eventos de DDoS");

        // 3) Volta para a visão completa
        dash.clearFilters();
        assertEquals(12, dash.getVisibleRowCount(), "visão completa novamente");

        // 4) Responde: bloqueia o IP do ataque de Força Bruta crítico
        dash.blockIp("203.0.113.10");
        assertEquals(1, dash.getBlocked(), "1 IP bloqueado");
        assertEquals(11, dash.getActive(), "11 eventos ainda ativos");
        assertTrue(dash.getNotificationText().contains("203.0.113.10"),
                "a notificação deveria confirmar o bloqueio");

        // 5) Reconhece outro evento que já está sendo tratado
        dash.acknowledgeIp("203.0.113.11");
        assertEquals("reconhecido", dash.getStatusForIp("203.0.113.11"), "evento reconhecido");
        assertEquals(10, dash.getActive(), "10 ativos após reconhecer mais 1");

        // 6) Chega um novo ataque durante o turno
        dash.simulateAttack();
        assertEquals(13, dash.getTotal(), "total sobe para 13");

        // 7) Localiza o novo ataque pela busca de IP
        dash.searchByIp("203.0.113.99");
        assertEquals(1, dash.getVisibleRowCount(), "apenas o novo ataque aparece");
        assertEquals("ativo", dash.getStatusForIp("203.0.113.99"), "novo ataque ainda ativo");
    }
}

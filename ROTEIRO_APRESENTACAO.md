# Roteiro de Apresentação — MonitorHack (Teste de Sistema Automatizado)

Tempo sugerido: **10 a 15 min**. Como todos do grupo precisam dominar o código,
abaixo há uma divisão de falas por integrante (ajuste para 2, 3 ou 4 pessoas).

---

## 0. Antes de começar (checklist)
- [ ] Chrome instalado e atualizado no laptop.
- [ ] Rodar `mvn test` **uma vez em casa** (baixa as dependências; na sala pode não ter internet).
- [ ] Deixar o Eclipse aberto no projeto e um terminal pronto.
- [ ] Testar o `mvn test -Dtest=EndToEndFlowTest` (é o que roda mais bonito no telão).

---

## 1. Abertura — o problema (≈1 min)
"Em um SOC, analistas monitoram ataques em tempo real. Toda vez que o sistema
muda, alguém precisa testar login, filtros, estatísticas e ações manualmente —
lento e sujeito a erro. Nós automatizamos esse teste de sistema de ponta a ponta."

## 2. Mostrar o sistema (SUT) rodando (≈2 min)
Abra `src/main/resources/app/index.html` no navegador (ou deixe um teste abrir).
Mostre ao vivo: login → tabela de ataques → filtrar por tipo → bloquear um IP →
as estatísticas mudando. "É **isto** que os testes vão verificar sozinhos."

## 3. Arquitetura — Page Object Model (≈2 min)
Explique o diagrama mental:

```
   Teste (o QUE verificar)  ->  Page Object (COMO interagir)  ->  Navegador  ->  SUT
   LoginTest, FilterTest        LoginPage, DashboardPage          Selenium       MonitorHack
```

- `pages/DashboardPage.java`: métodos como `filterByType`, `blockIp`, `getTotal`.
- Vantagem: se o HTML mudar, só mexemos no Page Object; os testes continuam iguais.

## 4. Os tipos de teste (≈3 min) — abra cada arquivo
1. **`LoginTest`** — teste simples + **parametrizado** (`@CsvSource`): várias
   credenciais inválidas em um método só.
2. **`AttackFilterTest`** — **data-driven**: cada linha do CSV é um tipo de ataque
   com a contagem esperada. Mostre a tabela de contagens (12 eventos no total).
3. **`DashboardStatsTest`** — **`@Nested`**: estado inicial vs. ações (bloquear/reconhecer).
4. **`EndToEndFlowTest`** — o fluxo completo do analista, passo a passo.

## 5. O diferencial — screenshot automático em falha (≈2 min) ⭐
Abra `extensions/ScreenshotOnFailureExtension.java`.
"Isto é uma **extensão do JUnit 5**. Ela roda depois de cada teste; se o teste
falhou, ela pega o navegador e salva um print em `target/screenshots/`. Resolve
um problema real: entender **por que** um teste de interface quebrou."

**Demonstração ao vivo (opcional e impactante):** mude um valor esperado para
errado (ex.: em `AttackFilterTest`, troque `"Malware, 1"` por `"Malware, 9"`),
rode, mostre o teste falhando e o **print gerado** na pasta. Depois desfaça.

## 6. Rodar a suíte no telão (≈2 min)
**Rode em MODO LENTO para a plateia acompanhar** (cada passo espera ~0,8s):

- No Eclipse: botão direito na classe > `Run As > Run Configurations…` > aba
  `Arguments` > em **VM arguments** coloque `-DdemoPauseMs=800`
  (use `1200` para ainda mais devagar). Depois `Run`.
- No terminal: `mvn test -Dtest=EndToEndFlowTest -DdemoPauseMs=800`

O Chrome abre e executa sozinho, passo a passo: login, filtro, bloqueio,
reconhecimento, novo ataque e busca. Ao final, mostre o `BUILD SUCCESS` e os
testes verdes.

## 7. Fechamento (≈1 min)
"Saímos do teste **unitário** (uma função isolada) para o teste de **sistema**
(o software inteiro, pela interface, como um usuário). Automatizar isso dá
confiança para mudar o sistema sem medo de quebrar o que já funcionava."

---

## Perguntas que a banca costuma fazer (e boas respostas)

- **Por que Selenium e não só JUnit?** JUnit organiza e executa os testes;
  Selenium controla o navegador de verdade. Juntos fazem teste de sistema de UI.
- **Diferença de teste unitário para teste de sistema?** Unitário testa uma
  unidade (método/classe) isolada; o de sistema testa o software completo
  integrado, pela interface, no fluxo do usuário.
- **Por que `WebDriverWait` e não `Thread.sleep`?** `sleep` trava um tempo fixo
  (lento e frágil); o `WebDriverWait` espera só até a condição acontecer.
- **O que é Page Object Model?** Padrão que encapsula cada tela em uma classe,
  separando a lógica de interação da lógica de verificação.
- **Como o ChromeDriver é configurado?** O Selenium Manager (Selenium 4.6+) baixa
  e aponta o driver automaticamente — por isso não há caminho de driver no código.
- **Os testes são confiáveis (não "flaky")?** Sim: dados iniciais fixos +
  esperas explícitas + navegador novo a cada teste = resultado determinístico.

---

## Divisão de falas sugerida
- **Integrante A:** seções 1, 2 e 7 (problema, demo do sistema, fechamento).
- **Integrante B:** seção 3 e 4.1–4.2 (arquitetura POM, login e filtros).
- **Integrante C:** seção 4.3–4.4 (nested e E2E).
- **Integrante D:** seção 5 e 6 (extensão de screenshot + rodar no telão).

*(Com 2 ou 3 pessoas, junte as seções — mas todos devem conseguir responder
qualquer pergunta da lista acima.)*

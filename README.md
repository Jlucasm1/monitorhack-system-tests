# MonitorHack — Teste de Sistema Automatizado

Projeto da disciplina para demonstrar **Teste de Sistema Automatizado (E2E)** com
**Selenium WebDriver + JUnit 5**, aplicado a um **painel de monitoramento de ataques**
de um Centro de Operações de Segurança (SOC).

> **Tema:** segurança da informação / monitoramento de ataques.
> **Stack:** Java 17 · Selenium 4 · JUnit 5 · Maven · IDE Eclipse.

---

## 1. O que é testado (o SUT)

O *System Under Test* é o **MonitorHack**, um painel web (HTML/CSS/JS puro, sem
backend) que simula o dia a dia de um analista de segurança:

- **Login** no SOC (controle de acesso);
- **Feed de eventos** de ataque: Força Bruta, SQL Injection, Port Scan, DDoS, XSS, Malware;
- **Cartões de estatística**: total, críticos, ativos e IPs bloqueados;
- **Filtros** por tipo, por severidade e **busca por IP**;
- **Ações**: *Bloquear IP*, *Reconhecer* evento e *Simular ataque*.

Os dados iniciais são fixos (12 eventos), o que torna cada teste **determinístico**.

## 2. O que a suíte de testes demonstra

| Recurso | Onde | Por que é relevante |
|---|---|---|
| **Page Object Model (POM)** | `pages/` | Separa a interação com a tela da verificação do teste |
| **Testes parametrizados (data-driven)** | `AttackFilterTest`, `LoginTest` | Cobre muitos cenários com pouco código (`@ParameterizedTest` + `@CsvSource`) |
| **Testes aninhados** | `DashboardStatsTest` (`@Nested`) | Organiza cenários relacionados |
| **Fluxo E2E ordenado** | `EndToEndFlowTest` | Simula a resposta real a um incidente |
| **Esperas explícitas** | `BasePage` / `DashboardPage` (`WebDriverWait`) | Testa uma UI **dinâmica** sem `Thread.sleep` |
| **Extensão de JUnit 5 — screenshot em falha** ⭐ | `extensions/ScreenshotOnFailureExtension` | **O diferencial:** tira print automático quando um teste quebra |
| **Servidor HTTP embutido** | `config/LocalAppServer` | Sobe o app numa URL `localhost`, como um teste de sistema real |
| **Driver auto-configurado** | `config/WebDriverFactory` | Selenium Manager baixa o ChromeDriver sozinho |

## 3. Pré-requisitos

- **JDK 17 ou superior** (`java -version`)
- **Maven** (ou usar o Maven embutido no Eclipse)
- **Google Chrome** instalado (o ChromeDriver é baixado automaticamente)
- Conexão com a internet na **primeira** execução (baixa Selenium e JUnit)

## 4. Como rodar

### Pela linha de comando

```bash
# roda toda a suíte com o navegador VISÍVEL (bom para a apresentação)
mvn test

# roda escondido (headless), útil se o data-show travar
mvn test -Dheadless=true

# roda só uma classe
mvn test -Dtest=EndToEndFlowTest

# MODO APRESENTAÇÃO: cada passo espera ~0,8s para a plateia acompanhar
mvn test -Dtest=EndToEndFlowTest -DdemoPauseMs=800
```

### No Eclipse

1. `File > Import… > Maven > Existing Maven Projects`
2. Selecione a pasta do projeto (onde está o `pom.xml`) e `Finish`.
3. Aguarde o Eclipse baixar as dependências.
4. Clique com o direito no projeto > `Run As > JUnit Test`
   (ou em uma classe específica dentro de `src/test/java`).

> Dica: para a apresentação, rodar `EndToEndFlowTest` sozinho conta uma
> história completa e o navegador aparece fazendo tudo na tela.

## 5. Estrutura do projeto

```
monitorhack/
├── pom.xml
├── README.md
├── ROTEIRO_APRESENTACAO.md
├── src/main/resources/app/        <- o SUT (painel que será testado)
│   ├── index.html
│   ├── styles.css
│   └── app.js
└── src/test/java/br/com/monitorhack/
    ├── config/        WebDriverFactory, LocalAppServer, HasDriver
    ├── extensions/    ScreenshotOnFailureExtension   (o "algo novo")
    ├── pages/         BasePage, LoginPage, DashboardPage  (Page Objects)
    └── tests/         BaseTest, LoginTest, DashboardStatsTest,
                       AttackFilterTest, EndToEndFlowTest
```

## 6. Onde caem os prints de falha

Se algum teste falhar, a imagem é salva em `target/screenshots/`. Para ver isso
funcionando na prática durante a apresentação, dá para forçar uma falha de
propósito (ver o roteiro).

---

*Projeto original criado para a disciplina — todo o código é próprio do grupo e
pode ser explicado linha a linha.*

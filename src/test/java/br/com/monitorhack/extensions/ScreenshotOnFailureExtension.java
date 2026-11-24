package br.com.monitorhack.extensions;

import br.com.monitorhack.config.HasDriver;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * *** O "algo novo" do projeto ***
 *
 * Extensão de JUnit 5 que captura automaticamente um print da tela do navegador
 * SEMPRE que um teste falha. O arquivo é salvo em target/screenshots/.
 *
 * Como funciona:
 *  - Implementa AfterTestExecutionCallback: o método abaixo roda logo após cada
 *    teste, com ou sem falha.
 *  - context.getExecutionException() só está presente quando o teste FALHOU.
 *  - Pegamos a instância do teste (que implementa HasDriver) para obter o driver
 *    e tirar o screenshot no exato estado em que a falha aconteceu.
 *
 * Isso demonstra um recurso avançado do JUnit 5 (extension model) e resolve um
 * problema real de QA: entender POR QUE um teste de UI quebrou.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        if (!failed) {
            return;
        }

        // Procura, entre as instâncias de teste (inclui a classe externa quando o
        // teste está dentro de uma classe @Nested), aquela que expõe o WebDriver.
        HasDriver holder = context.getTestInstances()
                .map(instances -> instances.getAllInstances().stream()
                        .filter(i -> i instanceof HasDriver)
                        .map(i -> (HasDriver) i)
                        .findFirst()
                        .orElse(null))
                .orElse(null);

        if (holder == null) {
            return;
        }

        WebDriver driver = holder.getDriver();
        if (!(driver instanceof TakesScreenshot)) {
            return;
        }

        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);

            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String name = context.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");
            Path dest = dir.resolve(name + "-" + stamp + ".png");

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), dest);

            System.out.println(">> [Screenshot da falha salvo em] " + dest.toAbsolutePath());
        } catch (Exception e) {
            System.err.println(">> Não foi possível salvar o screenshot: " + e.getMessage());
        }
    }
}

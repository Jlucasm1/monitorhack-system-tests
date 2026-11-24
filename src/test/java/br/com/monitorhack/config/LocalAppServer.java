package br.com.monitorhack.config;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servidor HTTP mínimo (usando o {@code com.sun.net.httpserver} embutido no JDK,
 * sem nenhuma dependência extra) que serve os arquivos estáticos do painel
 * (index.html, styles.css, app.js).
 *
 * Por que servir via HTTP em vez de abrir o arquivo com file://?
 * Porque é mais próximo de um teste de sistema REAL — o Selenium acessa o app
 * por uma URL http://localhost:porta, exatamente como um usuário faria.
 */
public class LocalAppServer {

    private HttpServer server;
    private int port;

    /** Sobe o servidor em uma porta livre escolhida pelo sistema operacional. */
    public void start() throws IOException {
        Path appDir = resolveAppDir();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> handle(exchange, appDir));
        server.setExecutor(null);
        server.start();
        this.port = server.getAddress().getPort();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public String getBaseUrl() {
        return "http://127.0.0.1:" + port + "/";
    }

    // ---------- interno ----------

    private void handle(HttpExchange exchange, Path appDir) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null || path.equals("/")) {
            path = "/index.html";
        }
        // Remove a barra inicial e normaliza, evitando "path traversal"
        Path file = appDir.resolve(path.substring(1)).normalize();
        if (!file.startsWith(appDir) || !Files.exists(file) || Files.isDirectory(file)) {
            byte[] notFound = "404".getBytes();
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(notFound); }
            return;
        }

        byte[] body = Files.readAllBytes(file);
        exchange.getResponseHeaders().add("Content-Type", contentType(file));
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }

    /**
     * Localiza a pasta do app. Primeiro tenta pelo classpath (funciona tanto no
     * Maven, que copia os recursos para target/classes/app, quanto no Eclipse,
     * que copia para bin/app). Como fallback, usa o caminho do código-fonte.
     */
    private Path resolveAppDir() {
        try {
            URL res = getClass().getClassLoader().getResource("app/index.html");
            if (res != null) {
                return Paths.get(res.toURI()).getParent();
            }
        } catch (Exception ignored) {
            // cai no fallback abaixo
        }
        return Paths.get("src", "main", "resources", "app").toAbsolutePath();
    }
}

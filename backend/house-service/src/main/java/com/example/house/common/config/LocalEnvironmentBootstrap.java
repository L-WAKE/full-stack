package com.example.house.common.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public final class LocalEnvironmentBootstrap {

    private static final int MYSQL_PORT = 3306;
    private static final int REDIS_PORT = 6379;
    private static final Duration START_TIMEOUT = Duration.ofSeconds(20);
    private static final String AUTO_START_ENV = "HOUSE_AUTO_START_LOCAL_ENV";

    private LocalEnvironmentBootstrap() {
    }

    public static void ensureDependenciesReady() {
        if (!shouldAutoStart()) {
            return;
        }

        if (isPortOpen(MYSQL_PORT) && isPortOpen(REDIS_PORT)) {
            return;
        }

        Path script = resolveStartScript();
        if (script == null) {
            return;
        }

        try {
            Process process = new ProcessBuilder(
                "powershell.exe",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                script.toString()
            )
                .directory(script.getParent().toFile())
                .inheritIO()
                .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Failed to start local house dependencies, exit code: " + exitCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to launch local dependency startup script.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting local dependencies.", e);
        }

        waitForPort("MySQL", MYSQL_PORT);
        waitForPort("Redis", REDIS_PORT);
    }

    private static boolean shouldAutoStart() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!osName.contains("windows")) {
            return false;
        }

        String env = System.getenv(AUTO_START_ENV);
        return env == null || Boolean.parseBoolean(env);
    }

    private static Path resolveStartScript() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("tools").resolve("start-house-env.ps1");
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static void waitForPort(String serviceName, int port) {
        Instant deadline = Instant.now().plus(START_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            if (isPortOpen(port)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for " + serviceName + " to start.", e);
            }
        }
        throw new IllegalStateException(serviceName + " did not become available on port " + port + ".");
    }

    private static boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}

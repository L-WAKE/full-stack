package com.example.house.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public final class LocalEnvironmentBootstrap {

    private static final int MYSQL_PORT = 3306;
    private static final int REDIS_PORT = 6379;
    private static final int SERVER_PORT = 8080;
    private static final Duration START_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration PROCESS_STOP_TIMEOUT = Duration.ofSeconds(5);
    private static final String AUTO_START_ENV = "HOUSE_AUTO_START_LOCAL_ENV";
    private static final String APP_IDENTIFIER = "com.example.house.HouseServiceApplication";
    private static final String PROJECT_IDENTIFIER = "backend\\house-service";

    private LocalEnvironmentBootstrap() {
    }

    public static void ensureDependenciesReady() {
        if (!shouldAutoStart()) {
            return;
        }

        if (isPortOpen(MYSQL_PORT) && isPortOpen(REDIS_PORT)) {
            ensureServerPortAvailable();
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
        ensureServerPortAvailable();
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

    private static void ensureServerPortAvailable() {
        Optional<Long> ownerPid = findOwningPid(SERVER_PORT);
        if (ownerPid.isEmpty()) {
            return;
        }

        long pid = ownerPid.get();
        ProcessHandle processHandle = ProcessHandle.of(pid)
            .orElseThrow(() -> new IllegalStateException("Port " + SERVER_PORT + " is in use by process " + pid + "."));

        if (!isCurrentProjectProcess(pid)) {
            throw new IllegalStateException(
                "Port " + SERVER_PORT + " is already in use by another process (PID " + pid + ")."
                    + " Stop that process or change server.port before starting house-service."
            );
        }

        processHandle.destroy();
        waitForProcessExit(processHandle, pid);
    }

    private static Optional<Long> findOwningPid(int port) {
        try {
            Process process = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-Command",
                "(Get-NetTCPConnection -LocalPort " + port
                    + " -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -First 1)"
            ).start();

            int exitCode = process.waitFor();
            String output = readProcessOutput(process.getInputStream()).trim();
            if (exitCode != 0 || output.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(output));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect port " + port + " owner.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while inspecting port " + port + " owner.", e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Failed to parse the process that owns port " + port + ".", e);
        }
    }

    private static String readProcessOutput(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static boolean isCurrentProjectProcess(long pid) {
        String commandLine = findProcessCommandLine(pid);
        return commandLine.contains(APP_IDENTIFIER) || commandLine.contains(PROJECT_IDENTIFIER);
    }

    private static String findProcessCommandLine(long pid) {
        try {
            Process process = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-Command",
                "(Get-CimInstance Win32_Process -Filter 'ProcessId = " + pid + "' | Select-Object -ExpandProperty CommandLine)"
            ).start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "";
            }
            return readProcessOutput(process.getInputStream()).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect command line for process " + pid + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while inspecting process " + pid + ".", e);
        }
    }

    private static void waitForProcessExit(ProcessHandle processHandle, long pid) {
        Instant deadline = Instant.now().plus(PROCESS_STOP_TIMEOUT);
        while (processHandle.isAlive() && Instant.now().isBefore(deadline)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while stopping the previous house-service process.", e);
            }
        }

        if (!processHandle.isAlive()) {
            waitForPortToClose(SERVER_PORT);
            return;
        }

        processHandle.destroyForcibly();
        try {
            processHandle.onExit().get();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop the previous house-service process (PID " + pid + ").", e);
        }
        waitForPortToClose(SERVER_PORT);
    }

    private static void waitForPortToClose(int port) {
        Instant deadline = Instant.now().plus(START_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            if (!findOwningPid(port).isPresent()) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for port " + port + " to be released.", e);
            }
        }
        throw new IllegalStateException("Port " + port + " is still in use after stopping the previous house-service process.");
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

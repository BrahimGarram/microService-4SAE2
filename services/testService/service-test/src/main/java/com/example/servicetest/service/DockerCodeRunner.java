package com.example.servicetest.service;

import com.example.servicetest.api.DiagnosticDto;
import com.example.servicetest.api.RunCodeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Exécute le code dans des conteneurs Docker (Java, Python, JavaScript).
 * Compare stdout au résultat attendu pour chaque cas de test ; ne renvoie jamais expectedOutput au client.
 */
@Component
public class DockerCodeRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerCodeRunner.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int RUN_TIMEOUT_SECONDS = 10;
    private static final int MAX_OUTPUT_LENGTH = 50_000;

    @Value("${code.execution.timeout-seconds:10}")
    private int timeoutSeconds = RUN_TIMEOUT_SECONDS;

    /**
     * Cas de test : input (stdin) et expectedOutput (comparé côté serveur uniquement).
     */
    public static class TestCase {
        public String input;
        public String expectedOutput;
    }

    /**
     * Exécute le code avec les cas de test et retourne le score (0-100) et les infos pour le client (sans exposer expectedOutput).
     */
    public RunCodeResponse run(String code, String language, String testCasesJson) {
        RunCodeResponse response = new RunCodeResponse();
        if (code == null || code.isBlank()) {
            response.setSuccess(false);
            response.setMessage("Empty code.");
            response.setScore(0);
            response.setTotalCount(0);
            response.setPassedCount(0);
            return response;
        }
        List<TestCase> cases = parseTestCases(testCasesJson);
        if (cases == null || cases.isEmpty()) {
            response.setSuccess(false);
            boolean hasJsonButInvalid = testCasesJson != null && !testCasesJson.isBlank();
            response.setMessage(hasJsonButInvalid
                    ? "Invalid or truncated test cases in database (check that test_cases_json column is LONGTEXT, not TINYTEXT)."
                    : "No test cases configured for this question.");
            response.setScore(0);
            response.setTotalCount(0);
            response.setPassedCount(0);
            return response;
        }

        String lang = (language == null ? "java" : language).toLowerCase();
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("code_");
            String fileName = getFileName(lang);
            Path codeFile = tempDir.resolve(fileName);
            Files.writeString(codeFile, code, Charset.forName("UTF-8"));

            int passed = 0;
            String firstStdout = null;
            String firstStderr = null;
            List<DiagnosticDto> diags = new ArrayList<>();

            for (int i = 0; i < cases.size(); i++) {
                TestCase tc = cases.get(i);
                RunResult runResult = runOneWithOutput(lang, tempDir, fileName, tc.input);
                if (runResult.compileError && runResult.stderr != null && !runResult.stderr.isEmpty()) {
                    if (firstStderr == null) firstStderr = runResult.stderr;
                    parseCompileErrors(lang, runResult.stderr, diags);
                }
                if (runResult.timeout) {
                    diags.add(new DiagnosticDto(0, "Timeout during execution (case " + (i + 1) + ").", "error"));
                }
                String normalizedOut = normalizeOutput(runResult.stdout);
                String normalizedExpected = normalizeOutput(tc.expectedOutput);
                if (normalizedExpected.equals(normalizedOut)) {
                    passed++;
                }
                if (firstStdout == null && runResult.stdout != null) firstStdout = runResult.stdout;
                if (firstStderr == null && runResult.stderr != null && !runResult.stderr.isEmpty()) firstStderr = runResult.stderr;
            }

            response.setSuccess(true);
            response.setPassedCount(passed);
            response.setTotalCount(cases.size());
            response.setScore(totalCountToScore(passed, cases.size()));
            response.setStdout(truncate(firstStdout));
            response.setStderr(truncate(firstStderr));
            response.setDiagnostics(diags);
            response.setMessage(passed + " / " + cases.size() + " test cases passed.");
        } catch (Exception e) {
            log.warn("Run code failed", e);
            response.setSuccess(false);
            response.setMessage("Execution error: " + (e.getMessage() != null ? e.getMessage() : "timeout or unavailable"));
            response.setScore(0);
            response.setTotalCount(cases.size());
            response.setPassedCount(0);
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                response.getDiagnostics().add(new DiagnosticDto(0, "Execution time limit exceeded.", "error"));
            }
        } finally {
            if (tempDir != null) {
                try {
                    deleteRecursively(tempDir.toFile());
                } catch (Exception e) {
                    log.debug("Cleanup temp dir failed", e);
                }
            }
        }
        return response;
    }

    private List<TestCase> parseTestCases(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        String trimmed = json.trim();
        if (trimmed.isEmpty()) return Collections.emptyList();
        try {
            return JSON.readValue(trimmed, new TypeReference<List<TestCase>>() {});
        } catch (Exception e) {
            log.warn("Parse testCasesJson failed (invalid or truncated JSON?): {} - length={}", e.getMessage(), json.length());
            return Collections.emptyList();
        }
    }

    private String getFileName(String lang) {
        switch (lang) {
            case "python": return "main.py";
            case "javascript": case "node": return "main.js";
            default: return "Main.java";
        }
    }

    private static class RunResult {
        boolean timeout;
        int exitCode;
        String stdout;
        String stderr;
        boolean compileError;
    }

    /** Capture stdout in a shared StringBuilder so we can get it after waitFor. */
    private RunResult runOneWithOutput(String lang, Path workDir, String fileName, String input) {
        String pathStr = workDir.toAbsolutePath().toString();
        if (File.separatorChar == '\\') {
            pathStr = pathStr.replace("\\", "/");
            if (pathStr.length() >= 2 && pathStr.charAt(1) == ':') {
                pathStr = "/" + pathStr.substring(0, 1).toLowerCase() + pathStr.substring(2);
            }
        }
        String image;
        List<String> cmd;
        switch (lang) {
            case "python":
                image = "python:3.11-alpine";
                cmd = Arrays.asList("docker", "run", "--rm", "-i", "--network=none", "--memory=128m",
                        "-v", pathStr + ":/workspace", "-w", "/workspace",
                        image, "python", fileName);
                break;
            case "javascript":
            case "node":
                image = "node:20-alpine";
                cmd = Arrays.asList("docker", "run", "--rm", "-i", "--network=none", "--memory=128m",
                        "-v", pathStr + ":/workspace", "-w", "/workspace",
                        image, "node", fileName);
                break;
            default:
                image = "eclipse-temurin:17-jdk-alpine";
                cmd = Arrays.asList("docker", "run", "--rm", "-i", "--network=none", "--memory=128m",
                        "-v", pathStr + ":/workspace", "-w", "/workspace",
                        image, "sh", "-c", "javac " + fileName + " 2>&1 && java Main");
                break;
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        pb.directory(workDir.toFile());
        Process p = null;
        StringBuilder outSb = new StringBuilder();
        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
        try {
            p = pb.start();
            final Process proc = p;
            Thread outT = new Thread(() -> {
                try (Reader r = new InputStreamReader(proc.getInputStream(), Charset.forName("UTF-8"))) {
                    char[] buf = new char[4096];
                    int n;
                    while ((n = r.read(buf)) != -1) outSb.append(buf, 0, n);
                } catch (IOException e) {
                    log.trace("stdout read", e);
                }
            });
            Thread errT = new Thread(() -> {
                try (InputStream in = proc.getErrorStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) errBaos.write(buf, 0, n);
                } catch (IOException e) {
                    log.trace("stderr read", e);
                }
            });
            outT.start();
            errT.start();
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = p.getOutputStream()) {
                    stdin.write(input.getBytes(Charset.forName("UTF-8")));
                    stdin.flush();
                }
            }
            p.getOutputStream().close();
            boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            outT.join(2000);
            errT.join(2000);
            String stderr = errBaos.toString(Charset.forName("UTF-8"));
            RunResult r = new RunResult();
            if (!finished) {
                p.destroyForcibly();
                r.timeout = true;
                r.stdout = outSb.toString();
                r.stderr = stderr;
                return r;
            }
            r.timeout = false;
            r.stdout = outSb.toString();
            r.stderr = stderr;
            r.exitCode = p.exitValue();
            r.compileError = (r.exitCode != 0);
            return r;
        } catch (Exception e) {
            if (p != null) p.destroyForcibly();
            throw new RuntimeException(e);
        }
    }

    private static String normalizeOutput(String s) {
        if (s == null) return "";
        return s.replace("\r\n", "\n").replace("\r", "\n").trim();
    }

    private static int totalCountToScore(int passed, int total) {
        if (total == 0) return 0;
        return Math.round((passed * 100f) / total);
    }

    private static String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= MAX_OUTPUT_LENGTH) return s;
        return s.substring(0, MAX_OUTPUT_LENGTH) + "\n... (tronqué)";
    }

    private static void parseCompileErrors(String lang, String stderr, List<DiagnosticDto> diags) {
        if (stderr == null) return;
        String[] lines = stderr.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int lineNum = 0;
            if (lang.equals("java") && line.contains(":")) {
                try {
                    String part = line.substring(0, line.indexOf(':'));
                    lineNum = Integer.parseInt(part.trim());
                } catch (Exception ignored) {}
            }
            diags.add(new DiagnosticDto(lineNum, line, "error"));
        }
        if (diags.isEmpty()) diags.add(new DiagnosticDto(0, stderr, "error"));
    }

    private static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursively(c);
        }
        f.delete();
    }
}

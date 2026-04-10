package com.example.servicetest.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Réponse d'exécution de code. On ne renvoie jamais la sortie attendue (expectedOutput) au client.
 */
public class RunCodeResponse {
    private boolean success;
    private int score;
    private int passedCount;
    private int totalCount;
    private String stdout;
    private String stderr;
    private List<DiagnosticDto> diagnostics = new ArrayList<>();
    private String message;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getPassedCount() { return passedCount; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public String getStdout() { return stdout; }
    public void setStdout(String stdout) { this.stdout = stdout; }
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    public List<DiagnosticDto> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(List<DiagnosticDto> diagnostics) { this.diagnostics = diagnostics != null ? diagnostics : new ArrayList<>(); }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

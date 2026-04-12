package com.example.servicetest.api;

/**
 * Erreur de compilation ou d'exécution (ligne, message). Sans exposer la sortie attendue.
 */
public class DiagnosticDto {
    private int line;
    private String message;
    private String severity;

    public DiagnosticDto() {}
    public DiagnosticDto(int line, String message, String severity) {
        this.line = line;
        this.message = message;
        this.severity = severity;
    }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}

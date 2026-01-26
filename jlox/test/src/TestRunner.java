package test;

import java.io.File;
import java.io.IOException;

public class TestRunner {
    private final File loxFile;
    private String output;
    private String error;
    private int exitCode;
    private final String classpath;

    public TestRunner(File loxFile, String classpath) {
        this.loxFile = loxFile;
        this.classpath = classpath;
    }

    public void run() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            "java", "-cp", classpath, "lox.Lox", loxFile.getAbsolutePath()
        );

        Process process = pb.start();

        // Capture stdout
        this.output = new String(process.getInputStream().readAllBytes());
        // Capture stderr
        this.error = new String(process.getErrorStream().readAllBytes());

        this.exitCode = process.waitFor();
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public File getLoxFile() {
        return loxFile;
    }
}

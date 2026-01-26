package lox;

import java.io.File;

public class TestResult {
    private final File testFile;
    private final boolean passed;
    private final String expected;
    private final String actual;
    private final String error;

    public TestResult(File testFile, boolean passed, String expected, String actual, String error) {
        this.testFile = testFile;
        this.passed = passed;
        this.expected = expected;
        this.actual = actual;
        this.error = error;
    }

    public File getTestFile() {
        return testFile;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }

    public String getError() {
        return error;
    }

    public void printResult() {
        if (passed) {
            System.out.println("✓ " + testFile.getName());
        } else {
            System.out.println("✗ " + testFile.getName());
            if (error != null && !error.isEmpty()) {
                System.out.println("  Error: " + error);
            }
            System.out.println("  Expected:");
            for (String line : expected.split("\n")) {
                System.out.println("    | " + line);
            }
            System.out.println("  Actual:");
            for (String line : actual.split("\n")) {
                System.out.println("    | " + line);
            }
        }
    }
}

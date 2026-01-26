package lox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoxTestFramework {
    private final String testFilesDir;
    private final String classpath;
    private List<TestResult> results = new ArrayList<>();

    public LoxTestFramework(String testFilesDir, String classpath) {
        this.testFilesDir = testFilesDir;
        this.classpath = classpath;
    }

    public void runAllTests() throws IOException, InterruptedException {
        File testDir = new File(testFilesDir);

        if (!testDir.exists() || !testDir.isDirectory()) {
            System.err.println("Test directory not found: " + testFilesDir);
            return;
        }

        File[] testFiles = testDir.listFiles((dir, name) -> name.endsWith(".lox"));

        if (testFiles == null || testFiles.length == 0) {
            System.out.println("No .lox files found in " + testFilesDir);
            return;
        }

        System.out.println("Running " + testFiles.length + " tests...\n");

        for (File testFile : testFiles) {
            runTest(testFile);
        }

        printSummary();
    }

    private void runTest(File testFile) throws IOException, InterruptedException {
        TestRunner runner = new TestRunner(testFile, classpath);
        runner.run();

        String expectedFile = testFile.getAbsolutePath().replace(".lox", ".expected");
        String expected = readExpectedOutput(expectedFile);

        if (expected == null) {
            // Generate expected output
            generateExpectedOutput(testFile, runner.getOutput());
            results.add(new TestResult(testFile, true, runner.getOutput(), runner.getOutput(),
                    "Generated new expected output"));
        } else {
            String actual = runner.getOutput();
            boolean passed = actual.equals(expected);
            results.add(new TestResult(testFile, passed, expected, actual, runner.getError()));
        }
    }

    private String readExpectedOutput(String expectedFile) {
        try {
            Path path = Paths.get(expectedFile);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            System.err.println("Error reading expected output: " + e.getMessage());
        }
        return null;
    }

    private void generateExpectedOutput(File testFile, String output) throws IOException {
        String expectedFile = testFile.getAbsolutePath().replace(".lox", ".expected");
        Files.writeString(Paths.get(expectedFile), output);
        System.out.println("Generated: " + expectedFile);
    }

    private void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Test Results");
        System.out.println("=".repeat(60) + "\n");

        long passed = results.stream().filter(TestResult::isPassed).count();
        long failed = results.size() - passed;

        for (TestResult result : results) {
            result.printResult();
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Summary: " + passed + " passed, " + failed + " failed out of " + results.size());
        System.out.println("=".repeat(60));

        if (failed > 0) {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        String testDir = "../testFiles";
        String classpath = "../lox/lox";

        if (args.length > 0) {
            testDir = args[0];
        }
        if (args.length > 1) {
            classpath = args[1];
        }

        try {
            LoxTestFramework framework = new LoxTestFramework(testDir, classpath);
            framework.runAllTests();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

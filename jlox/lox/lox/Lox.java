package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private static boolean printTree = false;

    public static void main(String[] args) throws IOException {
        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // System.out.println("\n=== Program terminated - Stack Trace ===");
        // Thread.currentThread().dumpStack();
        //
        // // Or print stack traces for all threads
        // System.out.println("\n=== All Thread Stack Traces ===");
        // Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        // for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
        // Thread thread = entry.getKey();
        // StackTraceElement[] stackTrace = entry.getValue();
        // System.out.println("\nThread: " + thread.getName());
        // for (StackTraceElement element : stackTrace) {
        // System.out.println("\tat " + element);
        // }
        // }
        // }));
        if (Arrays.asList(args).contains("--print")) {
            printTree = true;
        }

        if (args.length > 2) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length >= 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte bytes[] = Files.readAllBytes(Paths.get(path));
        String fileContents = new String(bytes, Charset.defaultCharset());
        run(fileContents);

        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    // The repl currently doesn't print, making it a rel, which just doesn't have
    // the nice ring to it.
    // Will work on this later after the implementation is done
    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            // We don't want an error to stop the session
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();
        if (printTree) {
            for (var stmt : stmts) {
                new PrettyPrinter().printStmt(stmt);
            }
        }

        if (hadError) {
            return;
        }
        interpreter.interpret(stmts);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line: " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line: " + error.token.line + "]");
        hadRuntimeError = true;

    }
}

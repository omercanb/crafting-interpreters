package lox;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Lox {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte bytes[] = Files.readAllBytes(Paths.get(path));
        String fileContents = new String(bytes, Charset.defaultCharset());
        run(fileContents);
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.println("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
        }
    }

    private static void run(String source) {
        // Scanner scanner = new Scanner(source);
        // List<Token> tokens = scanner.scanTokens();
        //
        // for (var token : tokens) {
        // System.out.println(token);
        // }
        System.out.println(source);

    }
}

package lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lox.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    // Used to check if break and continues are valid
    private int currentLoopNestingDepth = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        return stmts;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) {
                return varDeclaration();
            } else if (match(FUN)) {
                return function("function");

            } else {
                return statement();
            }
        } catch (

        ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after a variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) {
            return printStmt();
        } else if (match(IF)) {
            return ifStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(FOR)) {
            return forStatement();
        } else if (match(BREAK)) {
            return breakStatement();
        } else if (match(CONTINUE)) {
            return continueStatement();
        } else if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        } else {
            return exprStmt();
        }
    }

    private Stmt continueStatement() {
        if (currentLoopNestingDepth == 0) {
            error(previous(), "Continue statements only allowed in loops.");
        }
        consume(SEMICOLON, "Expected ';' after continue.");
        return new Stmt.Continue();
    }

    private Stmt breakStatement() {
        if (currentLoopNestingDepth == 0) {
            error(previous(), "Break statements only allowed in loops.");
        }
        consume(SEMICOLON, "Expected ';' after break.");
        return new Stmt.Break();
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after for.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = exprStmt();
        }

        Expr condition;
        if (match(SEMICOLON)) {
            condition = null;
        } else {
            condition = expression();
            consume(SEMICOLON, "Expected ';' after loop condition.");
        }

        Expr increment;
        if (match(RIGHT_PAREN)) {
            increment = null;
        } else {
            increment = expression();
            consume(RIGHT_PAREN, "Expect ')' after while condition.");
        }

        currentLoopNestingDepth++;
        Stmt body = statement();
        currentLoopNestingDepth--;

        return new Stmt.For(initializer, condition, increment, body);

        // // Desugar the for loop syntax into a while loop
        // // The new body becomes the body + increment
        // Stmt newBody;
        // if (increment == null) {
        // newBody = body;
        // } else {
        // newBody = new Stmt.Block(Arrays.asList(body, new
        // Stmt.Expression(increment)));
        // }
        //
        // // If the loop condition was empty it becomes while(true)
        // Expr newCondition;
        // if (condition == null) {
        // newCondition = new Expr.Literal(true);
        // } else {
        // newCondition = condition;
        // }
        // Stmt loop = new Stmt.While(newCondition, newBody);
        //
        // // We create the block the loop runs in and add the initializer if there is
        // one
        // Stmt loopBlock;
        // if (initializer == null) {
        // loopBlock = new Stmt.Block(Arrays.asList(loop));
        // } else {
        // loopBlock = new Stmt.Block(Arrays.asList(initializer, loop));
        // }
        //
        // return loopBlock;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after while.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        // Handle nesting depth for parsing break and continue statements
        currentLoopNestingDepth++;
        Stmt body = statement();
        currentLoopNestingDepth--;

        return new Stmt.While(condition, body);

    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after if.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = match(ELSE) ? statement() : null;
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "Semicolon expected after value.");
        return new Stmt.Print(expr);
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Semicolon expected after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        // Left hand side
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            // Is left hand side a valid assignment target
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        Expr right = null;
        while (match(OR)) {
            Token op = previous();
            right = and();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        Expr right = null;
        while (match(AND)) {
            Token op = previous();
            right = equality();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            Expr updatedExpr = new Expr.Binary(expr, operator, right);
            expr = updatedExpr;
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            Expr updatedExpr = new Expr.Binary(expr, operator, right);
            expr = updatedExpr;
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            Expr updatedExpr = new Expr.Binary(expr, operator, right);
            expr = updatedExpr;
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            Expr updatedExpr = new Expr.Binary(expr, operator, right);
            expr = updatedExpr;
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            Expr expr = new Expr.Unary(operator, right);
            return expr;
        } else {
            return call();
        }
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    // Parse the arguments for a function call and the closing paren
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                Expr arg = expression();
                arguments.add(arg);
                // Maybe we could throw a nice error if the comma is missing
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(TRUE)) {
            return new Expr.Literal(true);
        } else if (match(FALSE)) {
            return new Expr.Literal(false);
        } else if (match(NIL)) {
            return new Expr.Literal(null);
        } else if (match(NUMBER)) {
            return new Expr.Literal(previous().literal);
        } else if (match(STRING)) {
            return new Expr.Literal(previous().literal);
        } else if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        } else if (match(LEFT_PAREN)) {
            // Start of a parenthesized expression
            Expr expr = expression();
            // Expect a closing brace
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expected expression.");
    }

    // Used for panic mode recovery
    // Recovers up to a statement production and continues from there
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case TokenType.CLASS:
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN:
                    return;
                default:
                    break;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) {
            return advance();
        } else {
            throw error(peek(), errorMessage);
        }
    }

    private ParseError error(Token token, String message) {
        Lox.error(token.line, message);
        return new ParseError();
    }

    // Check if the current token is the queried type
    private boolean check(TokenType type) {
        if (peek().type == type) {
            return true;
        } else {
            return false;
        }
    }

    // Check if the current token is one of the queried types
    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

}

package lox;

import static lox.TokenType.*;

import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
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
            return primary();
        }
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
        } else if (match(LEFT_PAREN)) {
            // Start of a parenthesized expression
            Expr expr = expression();
            // Expect a closing brace
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression");
    }

    // Used for panic mode recovery
    // Recovers up to a statement production and continues from there
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
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
        return peek().type == EOF;
    }

}

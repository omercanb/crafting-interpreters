package loxplus;

import static loxplus.TokenType.*;

class PrettyPrinter implements Stmt.Visitor<R>, Expr.Visitor<String> {
    // Prints a (compound) expression
    void printExpr(Expr expr) {
        System.out.println(expr.accept(this));
    }

    @Override
    public String visitUnaryExpr(Expr.Unary unary) {
        return "(" + unary.op.lexeme + " " + unary.right.accept(this) + ")";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary binary) {
        return "(" + binary.op.lexeme + " " + binary.left.accept(this) + " " + binary.right.accept(this) + ")";
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping grouping) {
        return "(" + grouping.expr.accept(this) + ")";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal literal) {
        if (literal.value == null) {
            return "nil";
        }
        return literal.value.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(" + expr.name.lexeme + " = " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        new PrettyPrinter().printExpr(expression);
    }
}

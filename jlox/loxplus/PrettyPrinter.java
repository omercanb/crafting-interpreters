package loxplus;

class PrettyPrinter implements Expr.Visitor<String> {
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

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        new PrettyPrinter().printExpr(expression);
    }
}

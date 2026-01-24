package lox;

import java.util.List;

public abstract class Expr {
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);

        R visitBinaryExpr(Binary expr);

        R visitUnaryExpr(Unary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitVariableExpr(Variable expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Assign extends Expr {
        final Token name;
        final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Binary extends Expr {
        final Expr left;
        final Token op;
        final Expr right;

        Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Unary extends Expr {
        final Token op;
        final Expr right;

        Unary(Token op, Expr right) {
            this.op = op;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expr;

        Grouping(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }
}

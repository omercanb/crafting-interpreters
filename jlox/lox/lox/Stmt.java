package lox;

import java.util.List;

public abstract class Stmt {
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitIfStmt(If stmt);
        R visitBlockStmt(Block stmt);
    }
    abstract <R> R accept(Visitor<R> visitor);
    static class Expression extends Stmt {
        final Expr expr;

        Expression(Expr expr) {
            this.expr = expr; 
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }
    static class Print extends Stmt {
        final Expr expr;

        Print(Expr expr) {
            this.expr = expr; 
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }
    static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        Var(Token name, Expr initializer) {
            this.name = name; 
            this.initializer = initializer; 
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }
    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition; 
            this.thenBranch = thenBranch; 
            this.elseBranch = elseBranch; 
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }
    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements; 
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }
}

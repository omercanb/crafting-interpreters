package lox;

import static lox.TokenType.*;

import lox.Expr.Assign;
import lox.Expr.Call;
import lox.Expr.Logical;
import lox.Expr.Variable;
import lox.Stmt.Block;
import lox.Stmt.Break;
import lox.Stmt.Continue;
import lox.Stmt.Expression;
import lox.Stmt.For;
import lox.Stmt.Function;
import lox.Stmt.If;
import lox.Stmt.Print;
import lox.Stmt.Var;
import lox.Stmt.While;

class PrettyPrinter implements Stmt.Visitor<String>, Expr.Visitor<String> {
    // Prints a (compound) expression
    public void printExpr(Expr expr) {
        System.out.println(expr.accept(this));
    }

    public void printStmt(Stmt stmt) {
        System.out.println(stmt.accept(this));
    }

    @Override
    public String visitFunctionStmt(Function stmt) {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public String visitCallExpr(Call expr) {
        StringBuilder result = new StringBuilder("(" + expr.callee.accept(this) + " ");
        for (Expr arg : expr.arguments) {
            result.append(arg.accept(this)).append(" ");
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String visitBlockStmt(Block stmt) {
        StringBuilder result = new StringBuilder("{ ");
        for (Stmt s : stmt.statements) {
            result.append(s.accept(this)).append(" ");
        }
        result.append("}");
        return result.toString();
    }

    @Override
    public String visitForStmt(For stmt) {
        StringBuilder result = new StringBuilder("(for ");
        if (stmt.initializer != null) {
            result.append(stmt.initializer.accept(this) + " ");
        }
        result.append(stmt.condition.accept(this) + " ");
        if (stmt.increment != null) {
            result.append(stmt.increment.accept(this) + " ");
        }
        result.append(stmt.body.accept(this) + ")");
        return result.toString();
    }

    @Override
    public String visitBreakStmt(Break stmt) {
        return "break";
    }

    @Override
    public String visitContinueStmt(Continue stmt) {
        return "continue";
    }

    @Override
    public String visitExpressionStmt(Expression stmt) {
        return stmt.expr.accept(this);
    }

    @Override
    public String visitIfStmt(If stmt) {
        String result = "(if " + stmt.condition.accept(this) + " " + stmt.thenBranch.accept(this);
        if (stmt.elseBranch != null) {
            result += " " + stmt.elseBranch.accept(this);
        }
        result += ")";
        return result;
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        return "(print " + stmt.expr.accept(this) + ")";
    }

    @Override
    public String visitVarStmt(Var stmt) {
        String result = "(var " + stmt.name.lexeme;
        if (stmt.initializer != null) {
            result += " = " + stmt.initializer.accept(this);
        }
        result += ")";
        return result;
    }

    @Override
    public String visitWhileStmt(While stmt) {
        return "(while " + stmt.condition.accept(this) + " " + stmt.body.accept(this) + ")";
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
    public String visitAssignExpr(Assign expr) {
        return "(" + expr.name.lexeme + " = " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        return "(" + expr.op.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this) + ")";
    }

    @Override
    public String visitVariableExpr(Variable expr) {
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

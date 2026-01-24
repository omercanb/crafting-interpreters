package lox;

import java.util.List;

import static lox.TokenType.*;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    void interpret(List<Stmt> stmts) {
        try {
            for (var stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Void visitPrintStmt(Stmt.Print printStmt) {
        System.out.println(stringify(evaluate(printStmt.expr)));
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.Expression exprStmt) {
        evaluate(exprStmt.expr);
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping grouping) {
        return evaluate(grouping.expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        // The unary expressions are logical negation (!) and additive negation (-)
        Object right = evaluate(unary);
        switch (unary.op.type) {
            case TokenType.MINUS:
                checkNumberOperand(unary.op, right);
                return -(Double) right;
            case TokenType.BANG:
                return !isTruthy(right);
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        switch (binary.op.type) {
            case TokenType.PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                } else {
                    throw new RuntimeError(binary.op, "Operands must be two numbers or two strings");
                }
            case TokenType.MINUS:
                checkNumberOperands(binary.op, left, right);
                return (double) left - (double) right;
            case TokenType.STAR:
                checkNumberOperands(binary.op, left, right);
                return (double) left * (double) right;
            case TokenType.SLASH:
                checkNumberOperands(binary.op, left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(binary.op, "Divide by zero");
                }
                return (double) left / (double) right;
            case TokenType.LESS:
                checkNumberOperands(binary.op, left, right);
                return (double) left < (double) right;
            case TokenType.LESS_EQUAL:
                checkNumberOperands(binary.op, left, right);
                return (double) left <= (double) right;
            case TokenType.GREATER:
                checkNumberOperands(binary.op, left, right);
                return (double) left > (double) right;
            case TokenType.GREATER_EQUAL:
                checkNumberOperands(binary.op, left, right);
                return (double) left >= (double) right;
            case TokenType.EQUAL_EQUAL:
                return isEqual(left, right);
            case TokenType.BANG_EQUAL:
                return !isEqual(left, right);
            default:
                break;
        }
        return null;
    }

    private boolean isTruthy(Object object) {
        // False and Null are falsy, everything else is truthy, like in Ruby
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null) {
            if (right == null) {
                return true;
            } else {
                return false;
            }
        }
        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (!(operand instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number");
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}

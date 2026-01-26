package lox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    private boolean loopBreak = false;
    private boolean loopContinue = false;

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
    public Object visitCallExpr(Expr.Call callExpr) {
        Object callee = evaluate(callExpr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr arg : callExpr.arguments) {
            arguments.add(evaluate(arg));
        }

        // "notcallable"()
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(callExpr.paren, "Can only call functions and classes.");
        }
        LoxCallable function = (LoxCallable) callee;

        // Arity checking is here instead of inside concrete implementations because
        // both functions and classes need to implement this logic.
        if (function.arity() != arguments.size()) {
            throw new RuntimeError(callExpr.paren,
                    "Expected " + function.arity() + " arguments, but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Void visitForStmt(Stmt.For forStmt) {
        // Create a new scope for the for loop
        Environment previous = environment;
        environment = new Environment(previous);
        try {
            if (forStmt.initializer != null) {
                execute(forStmt.initializer);
            }
            if (forStmt.initializer != null) {
                while (isTruthy(evaluate(forStmt.condition)) && !loopBreak) {
                    execute(forStmt.body);
                    evaluate(forStmt.increment);
                    loopContinue = false;
                }
            } else {
                while (isTruthy(evaluate(forStmt.condition)) && !loopBreak) {
                    execute(forStmt.body);
                    loopContinue = false;
                }
            }
            if (loopBreak) {
                loopBreak = false;
            }
        } finally {
            environment = previous;
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While whileStmt) {
        while (isTruthy(evaluate(whileStmt.condition)) && !loopBreak) {
            execute(whileStmt.body);
            loopContinue = false;
        }
        if (loopBreak) {
            loopBreak = false;
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        loopBreak = true;
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        loopContinue = true;
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical logicalExpr) {
        // Make sure to evaluate the left expression only once
        Object leftVal = evaluate(logicalExpr.left);
        // We short circuit the right value
        if (logicalExpr.op.type == TokenType.AND) {
            if (isTruthy(leftVal)) {
                return evaluate(logicalExpr.right);
            } else {
                // The left val is falsy and we return falsy
                return leftVal;
            }
        } else {
            if (isTruthy(leftVal)) {
                // The left val is truthy and we return truthy
                return leftVal;
            } else {
                return evaluate(logicalExpr.right);
            }
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If ifStmt) {
        if (isTruthy(evaluate(ifStmt.condition))) {
            execute(ifStmt.thenBranch);
        } else if (ifStmt.elseBranch != null) {
            execute(ifStmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        // Create new child scope
        executeBlock(block.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var varStmt) {
        Object value = null;
        if (varStmt.initializer != null) {
            value = evaluate(varStmt.initializer);
        }

        environment.define(varStmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print printStmt) {
        System.out.println(stringify(evaluate(printStmt.expr)));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign assignExpr) {
        // assignExpr.value is the right hand side expression
        Object exprValue = evaluate(assignExpr.value);
        environment.assign(assignExpr.name, exprValue);
        return exprValue;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
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
            case MINUS:
                checkNumberOperand(unary.op, right);
                return -(Double) right;
            case BANG:
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
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String || left instanceof Double && right instanceof String
                        || right instanceof Double) {
                    // If both are not numbers but if they are a combination of strings and numbers,
                    // convert to string.

                    return stringify(left) + stringify(right);
                } else {
                    throw new RuntimeError(binary.op, "Operands must be two numbers or two strings.");
                }
            case MINUS:
                checkNumberOperands(binary.op, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(binary.op, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(binary.op, left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(binary.op, "Divide by zero.");
                }
                return (double) left / (double) right;
            case LESS:
                checkNumberOperands(binary.op, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(binary.op, left, right);
                return (double) left <= (double) right;
            case GREATER:
                checkNumberOperands(binary.op, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(binary.op, left, right);
                return (double) left >= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
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
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            double number = (double) object;
            if (number == Math.floor(number)) {
                return String.format("%.0f", number);
            } else {
                return String.format("%f", number);
            }
        }

        return object.toString();
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (var stmt : statements) {
                execute(stmt);
                if (loopContinue || loopBreak) {
                    return;
                }
            }
        } finally {
            this.environment = previous;
        }
    }

    private void execute(Stmt stmt) {
        if (loopContinue || loopBreak) {
            return;
        }
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}

class BreakException extends Exception {
}

class ContinueException extends Exception {
}

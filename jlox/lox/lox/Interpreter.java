package lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

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
    public Void visitWhileStmt(Stmt.While whileStmt) {
        while (isTruthy(evaluate(whileStmt.condition))) {
            execute(whileStmt.body);
        }
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

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (var stmt : statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
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

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
}

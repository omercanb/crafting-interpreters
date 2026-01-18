package lox;

class Interpreter implements Expr.Visitor<Object> {

    void interpret(Expr expression) {
        try {
            Object value = expression.accept(this);
            System.out.println(value);
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping grouping) {
        return grouping.expr.accept(this);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        // The unary expressions are logical negation (!) and additive negation (-)
        Object right = unary.right.accept(this);
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
        Object left = binary.left.accept(this);
        Object right = binary.right.accept(this);

        switch (binary.op.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                } else {
                    throw new RuntimeError(binary.op, "Operands must be two numbers or two strings");
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
                    throw new RuntimeError(binary.op, "Divide by zero");
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
            throw new RuntimeError(operator, "Operand must be a number");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number");
        }
    }

}

from plox import expr as expr_module
from plox import stmt as stmt_module


class PrettyPrinter:
    def print_stmt(self, statement) -> None:
        print(self.visit(statement))

    def print_expr(self, expression) -> None:
        print(self.visit(expression))

    def visit(self, node) -> str:
        method_name = f"visit_{type(node).__name__}"
        method = getattr(self, method_name, None)
        if method is None:
            raise NotImplementedError(f"No visitor for {type(node).__name__}")
        return method(node)

    # Expression visitors
    def visit_Assign(self, node) -> str:
        return self.parenthesize(f"assign {node.name.lexeme}", node.value)

    def visit_Binary(self, node) -> str:
        return self.parenthesize(node.op.lexeme, node.left, node.right)

    def visit_Logical(self, node) -> str:
        return self.parenthesize(node.op.lexeme, node.left, node.right)

    def visit_Unary(self, node) -> str:
        return self.parenthesize(node.op.lexeme, node.right)

    def visit_Grouping(self, node) -> str:
        return self.parenthesize("group", node.expr)

    def visit_Literal(self, node) -> str:
        if node.value is None:
            return "nil"
        return str(node.value)

    def visit_Variable(self, node) -> str:
        return node.name.lexeme

    def visit_Call(self, node) -> str:
        result = self.parenthesize("call", node.callee, *node.arguments)
        return result

    # Statement visitors
    def visit_Expression(self, node) -> str:
        return self.visit(node.expr)

    def visit_Print(self, node) -> str:
        return self.parenthesize("print", node.expr)

    def visit_Var(self, node) -> str:
        if node.initializer is None:
            return f"(var {node.name.lexeme})"
        return f"(var {node.name.lexeme} = {self.visit(node.initializer)})"

    def visit_If(self, node) -> str:
        if node.else_branch is None:
            return self.parenthesize("if", node.condition, node.then_branch)
        return self.parenthesize("if", node.condition, node.then_branch, node.else_branch)

    def visit_While(self, node) -> str:
        return self.parenthesize("while", node.condition, node.body)

    def visit_For(self, node) -> str:
        parts = [node.initializer, node.condition, node.increment, node.body]
        return self.parenthesize("for", *[p for p in parts if p is not None])

    def visit_Function(self, node) -> str:
        return ""

    def visit_Break(self, node) -> str:
        return "break"

    def visit_Continue(self, node) -> str:
        return "continue"

    def visit_Block(self, node) -> str:
        items = " ".join(self.visit(stmt) for stmt in node.statements)
        return f"{{ {items} }}"

    def parenthesize(self, name: str, *exprs) -> str:
        result = f"({name}"
        for expr in exprs:
            if expr is None:
                continue
            result += f" {self.visit(expr)}"
        result += ")"
        return result

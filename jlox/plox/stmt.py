from dataclasses import dataclass
from typing import Any, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from plox.expr import Expr
    from plox.lox_token import Token


class Stmt:
    pass


@dataclass
class Expression(Stmt):
    expr: "Expr"


@dataclass
class Print(Stmt):
    expr: "Expr"


@dataclass
class Var(Stmt):
    name: "Token"
    initializer: Optional["Expr"]


@dataclass
class If(Stmt):
    condition: "Expr"
    then_branch: Stmt
    else_branch: Optional[Stmt]


@dataclass
class While(Stmt):
    condition: "Expr"
    body: Stmt


@dataclass
class For(Stmt):
    initializer: Optional[Stmt]
    condition: Optional["Expr"]
    increment: Optional["Expr"]
    body: Stmt


@dataclass
class Function(Stmt):
    name: "Token"
    params: list
    body: list


@dataclass
class Break(Stmt):
    pass


@dataclass
class Continue(Stmt):
    pass


@dataclass
class Block(Stmt):
    statements: list

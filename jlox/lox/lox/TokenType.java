package lox;

enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, PLUS, MINUS, SEMICOLON, SLASH, STAR,

    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    TRUE, FALSE,
    AND, OR,
    IF, ELSE,
    FOR, WHILE,
    FUN, RETURN,
    VAR,
    CLASS, THIS, SUPER, NIL,
    PRINT,

    EOF

}

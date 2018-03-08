# CS411Project2

In ToyLexScanner.java
change import java_cup.runtime.*;
To: 
import java_cup.runtime.Symbol;
import java_cup.runtime.lr_parser;

    // Connect this parser to a scanner!
    ToyLexScanner lexer;
    parser(ToyLexScanner lexer){ this.lexer = lexer; }

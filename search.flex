/*
* CS 411 Project 1 Lexical Analyzer
* Name: Gerianna Geminiano, Andrew Quach
*/
import java.io.*;

%%
%class ToyLexScanner
%unicode
%cup
%line
%column

%init{
    Trie dataTrie = new Trie(500);
    createFile();

%init}

%{
    private Symbol symbol(int type)
    {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value)
    {
        return new Symbol(type, yyline, yycolumn, value);
    }


    // Print trie table
    public void printTrie()
    {
      dataTrie.print("OUTPUT.txt");
    }

  // Print the tokens as their associated integers
    public void printLexerOutput()
    {
        writeTo(lexerOutput);
    }

  // Overwrite file OUTPUT.txt
  public void createFile(){
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("OUTPUT.txt",false))) {
    bw.write("");
    bw.close();
    } catch (IOException e) {
        e.printStackTrace();
        }
  }

  //Append to file OUTPUT.txt
  public void writeTo(String token){
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("OUTPUT.txt",true))) {
			bw.write(token);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
%}

Letter = [a-zA-Z]
Digit = [0-9]
UnderScore = "_"

// ID = a Letter followed by a sequene of 0 or more Letters, Digits, or Underscores,
Identifier = {Letter}({Letter}|{Digit}|{UnderScore})*

// Decimal = 0 or [1-9] followed by a sequnce zero or more digits
DecInteger= 0 | [1-9][0-9]*

// Hex = 0x or 0X followed by 1 or more digits or the letters a-f or A-F
HexInteger = 0[xX][0-9A-Fa-f]+

// Integer can be in decimal or hexadecimal
Integer = {DecInteger}|{HexInteger}

/*Double = at least 1 digit ([0-9]+) followed by a period (\.)
 followed by zero or more digits ([0-9]*). The exponent is optional (?).
 Sign of exponent is optional ([\+\-]?). Exponent can be upper or lower
 case ([eE]). One or more digits must follow exponent ([0-9]+)
 */
DoubleConst= [0-9]+\.[0-9]*([eE][\+\-]?[0-9]+)?

// White space can be blank or tabs
WhiteSpace = [ \t]+

// White space can also be new line/end of line
EndOfLine = \r|\n|\r\n

// Single Line comment cannt include return or newline inside ([^\r\n])
CommentChar = [^\r\n]
// SingleLineComment = // followed by valid charcter and stops when there an end of line char
SingleLineComment = "//" {CommentChar}* {EndOfLine}?

// MultLineComment = /* followed by anything until (~) you reach */  OR
// /* with anything but */ in the middle and ends with */ (no nesting)
MultiLineComment = "/*" ~"*/" | "/*" [^"*/"]* "*/"

// Comment is either single line or multi line
Comment = {MultiLineComment} | {SingleLineComment}

// String cannot include a new line (\r\n) or double qoute (")
StringChar = [^\r\n\"\\. ]

%state STRING
%%

<YYINITIAL> {

  /* KEYWORDS */
  boolean         { dataTrie.insert(yytext());
                    return symbol(sym.BOOL);}
  break           { dataTrie.insert(yytext());
                    return symbol(sym.BREAK);}
  class           { dataTrie.insert(yytext());
                    return symbol(sym.CLASS);}
  double          { dataTrie.insert(yytext());
                    return symbol(sym.DOUBLE);}
  else            { dataTrie.insert(yytext());
                    return symbol(sym.ELSE);}
  extends         { dataTrie.insert(yytext());
                    return symbol(sym.EXTENDS);}
  for             { dataTrie.insert(yytext());
                    return symbol(sym.FOR);}
  if              { dataTrie.insert(yytext());
                    return symbol(sym.IF);}
  implements      { dataTrie.insert(yytext());
                    return symbol(sym.IMPLEMENTS);}
  int             { dataTrie.insert(yytext());
                    return symbol(sym.INT);}
  interface       { dataTrie.insert(yytext());
                    return symbol(sym.INTERFACE);}
  newarray        { dataTrie.insert(yytext());
                    return symbol(sym.NEWARRAY);}
  println         { dataTrie.insert(yytext());
                    return symbol(sym.PRINTLN);}
  readln          { dataTrie.insert(yytext());
                    return symbol(sym.READLN);}
  return          { dataTrie.insert(yytext());
                    return symbol(sym.RETURN);}
  string          { dataTrie.insert(yytext());
                    return symbol(sym.STRING);}
  void            { dataTrie.insert(yytext());
                    return symbol(sym.VOID);}
  while           {  dataTrie.insert(yytext());
                    return symbol(sym.WHILE);}

  /* BOOLEAN CONSTANT */
  true            { dataTrie.insert(yytext());
                    return symbol(sym.BOOL_CONST, yytext());}
  false           { dataTrie.insert(yytext());
                    return symbol(sym.BOOL_CONST, yytext());}

  /* Errors for Illegal Char */
  "~"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "@"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "#"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "$"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "^"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "|"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "?"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  ":"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }
  "'"  { throw new RuntimeException("Illegal character \""+yytext()+"\""); }

  /* IDENTIFIER */
  {Identifier}    { dataTrie.insert(yytext());
                    return symbol(sym.ID);}

  /* ILLEGAL IDENTIFIER */
  {UnderScore}({Letter}|{Digit}|{UnderScore})+  { throw new RuntimeException("Illegal identifier\""+yytext()+"\""); }

  /* INTEGER CONSTANT*/
  {Integer}       { return symbol(sym.INT_CONST);}

  /* DOUBLE CONSTANT */
  {DoubleConst}   {return symbol(sym.DOUBLE_CONST);}

  /* OPERATORS and PUNCTUATIONS*/
  "+"             {return symbol(sym.PLUS);}
  "-"             {return symbol(sym.MINUS);}
  "*"             {return symbol(sym.MULTI);}
  "/"             {return symbol(sym.DIVIDE);}
  "%"             {return symbol(sym.MOD);}
  "<"             {return symbol(sym.LESS);}
  "<="            {return symbol(sym.LESS_EQ);}
  ">"             {return symbol(sym.GTR);}
  ">="            {return symbol(sym.GTR_EQ);}
  "=="            {return symbol(sym.EQ);}
  "!="            {return symbol(sym.NOT_EQ);}
  "&&"            {return symbol(sym.AND);}
  "||"            {return symbol(sym.OR);}
  "!"             {return symbol(sym.NOT);}
  "="             {return symbol(sym.ASSIGN);}
  ";"             {return symbol(sym.SEMI);}
  ","             {return symbol(sym.COMMA);}
  "."             {return symbol(sym.PERIOD);}
  "("             {return symbol(sym.LEFT_PAREN);}
  ")"             {return symbol(sym.RIGHT_PAREN);}
  "["             {return symbol(sym.LEFT_BRKT);}
  "]"             {return symbol(sym.RIGHT_BRKT);}
  "{"             {return symbol(sym.LEFT_BRACE);}
  "}"             {return symbol(sym.RIGHT_BRACE);}

  /* STRING CONSTANT */
  // Begin checking string by going to state STRING
  \"              {yybegin(STRING);}

  /* COMMENTS */
  {Comment}       {writeTo("\n");}

  {WhiteSpace}    { /* do nothing */}

  \n              {writeTo("\r\n");}

  .               { /* do nothing */}
}

<STRING> {
  /* ERRORS */
  \\.             { throw new RuntimeException("Illegal new line \""+yytext()+"\" in string."); }
  {EndOfLine}     {throw new RuntimeException("Unterminated string at end of line"); }

  /* END OF STRING */
  // Go back to inital state and read as normal
  \"              { yybegin(YYINITIAL); return symbol(sym.STRING_CONST);}

  /* STRING CHARACTERS */
  {StringChar}+   { }

  /* escape sequences */
  "\\b"           { }
  "\\t"           { }
  "\\n"           { }
  "\\f"           { }
  "\\r"           { }
  "\\\""          { }
  "\\'"           { }
  "\\\\"          { }
}

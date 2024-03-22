/* SPECIFICHE LESSICALI

   VAR "var"
   COLON ":"
   ASSIGN "^="
   SEMI ";"
   ID espressione per identificatore
   COMMA ","
   REAL_CONST espressione per numero reale
   INTEGER_CONST  espressione per numero intero
   STRING_CONST   espressione per stringa costante
   TRUE "true"
   FALSE "false"
   REAL "real"
   INTEGER "integer"
   STRING "string"
   BOOLEAN "boolean"
   RETURN "return"
   FUNCTION "func"
   TYPERETURN "->"
   ENDFUNCTION "endfunc"
   LPAR "("
   RPAR ")"
   PROCEDURE "proc"
   ENDPROCEDURE "endproc"
   OUT "out"
   WRITE "-->"
   WRITERETURN "-->!"
   DOLLARSIGN "$"
   READ "<--"
   IF "if"
   THEN "then"
   ELSE "else"
   ENDIF "endif"
   ELIF "elseif"
   WHILE "while"
   DO "do"
   ENDWHILE "endwhile"
   PLUS '+'
   MINUS '-'
   TIMES '*'
   DIV '/'
   EQ '='
   NE '<>'
   LT '<'
   LE '<='
   GT '>'
   GE '>='
   AND '&&'
   OR '||'
   NOT '!'
   ENDVAR '\'
   REF '@'

   I commenti vanno racchiusi fra % di apertura e % di chiusura
   Bisogna dare errore di
   - "Stringa costante non completata" nel caso il programma input presenti una stringa
   costante aperta ma non chiusa (es. "questa è una stringa non chiusa ).
   - "Commento non chiuso" nel caso il programma input presenti un commento non chiuso
   (es. /* questo è un commento non chiuso )

   Nota: in entrambi i casi si raggiunge l'EOF mentre si sta riconoscendo un commento
   o una stringa. Se si usano gli stati jflex (ad es. COMMENT e STRING), questo si
   traduce nell'incontrare un EOF mentre si è nel corrispondente stato.
*/
package esercitazione5;
import java_cup.runtime.Symbol;

%%
%class Lexer
%cup
%line
%column

%{
    StringBuffer string = new StringBuffer();
    int maxCharLength = 1;
    int startComment;
    int startString;
    private Symbol symbol(int type)
    {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value)
    {
        return new Symbol(type, yyline, yycolumn, value);
    }

    // Metodo per gestire errori di stringhe non chiuse
    private void handleUnclosedStringError() {
        throw new Error("Stringa costante non completata alla linea " + startString);
    }

    // Metodo per gestire errori di commenti non chiusi
    private void handleUnclosedCommentError() {
        throw new Error("Commento non chiuso alla linea " + startComment);
    }

%}

%eof{
    if(yystate() == STRING)
        handleUnclosedStringError();
    else if (yystate() == COMMENT)
        handleUnclosedCommentError();
%eof}



WhiteSpace = {LineTerminator} | [ \t\f]
LineTerminator = \r | \n | \r\n
InputChar = [^\r\n]

Comment = "%" [^*] ~"%"

Identifier = {letter} ({letter} | {digit} | {underscore})*
RealNumber = {digit}+(\.{digit}+)
IntegerNumber = {digit}+
letter = [A-Za-z]
digit = [0-9]
underscore = [_]

%state STRING
%state CHAR
%state COMMENT
%%

<YYINITIAL>
{
/* keywords */
"var"                 {return symbol(sym.VAR);}
"true"                {return symbol(sym.TRUE);}
"false"               {return symbol(sym.FALSE);}
"return"              {return symbol(sym.RETURN);}
"func"                {return symbol(sym.FUNCTION);}
"endfunc"             {return symbol(sym.ENDFUNCTION);}
"proc"                {return symbol(sym.PROCEDURE);}
"endproc"             {return symbol(sym.ENDPROCEDURE);}
"out"                 {return symbol(sym.OUT);}
"func"                {return symbol(sym.FUNCTION);}
"if"                  {return symbol(sym.IF);}
"then"                {return symbol(sym.THEN);}
"else"                {return symbol(sym.ELSE);}
"endif"               {return symbol(sym.ENDIF);}
"elseif"              {return symbol(sym.ELIF);}
"while"               {return symbol(sym.WHILE);}
"do"                  {return symbol(sym.DO);}
"endwhile"            {return symbol(sym.ENDWHILE);}
"integer"             {return symbol(sym.INTEGER);}
"real"                {return symbol(sym.REAL);}
"string"              {return symbol(sym.STRING);}
"boolean"             {return symbol(sym.BOOLEAN);}
"char"                {return symbol(sym.CHAR);}
"for"                 {return symbol(sym.FOR);}
"to"                  {return symbol(sym.TO);}
"step"                {return symbol(sym.STEP);}
"endfor"              {return symbol(sym.ENDFOR);}


{Identifier}          {return symbol(sym.ID, yytext());}

{IntegerNumber}       {return symbol(sym.INTEGER_CONST, yytext());}
{RealNumber}          {return symbol(sym.REAL_CONST, yytext());}
\'                    {string.setLength(0); startString = yyline+1; yybegin(CHAR);}
\"                    {string.setLength(0); startString = yyline+1; yybegin(STRING);}
"%"                   {startComment = yyline+1; yybegin(COMMENT);}


/* operators */
"^="                  {return symbol(sym.ASSIGN);}
"+"                   {return symbol(sym.PLUS);}
"-"                   {return symbol(sym.MINUS);}
"*"                   {return symbol(sym.TIMES);}
"/"                   {return symbol(sym.DIV);}
"="                   {return symbol(sym.EQ);}
"<>"                  {return symbol(sym.NE);}
"<"                   {return symbol(sym.LT);}
"<="                  {return symbol(sym.LE);}
">"                   {return symbol(sym.GT);}
">="                  {return symbol(sym.GE);}
"&&"                  {return symbol(sym.AND);}
"||"                  {return symbol(sym.OR);}
"!"                   {return symbol(sym.NOT);}




/* separators */
";"                   {return symbol(sym.SEMI);}
","                   {return symbol(sym.COMMA);}
"("                   {return symbol(sym.LPAR);}
")"                   {return symbol(sym.RPAR);}


":"                   {return symbol(sym.COLON);}
"->"                  {return symbol(sym.TYPERETURN);}
"-->"                 {return symbol(sym.WRITE);}
"-->!"                {return symbol(sym.WRITERETURN);}
"$"                   {return symbol(sym.DOLLARSIGN);}
"<--"                 {return symbol(sym.READ);}
"\\"                  {return symbol(sym.ENDVAR);}
"@"                   {return symbol(sym.REF);}


{Comment}             {;}
{WhiteSpace}          {;}

}

<STRING>
{

\"                    {yybegin(YYINITIAL); return symbol(sym.STRING_CONST, string.toString());}

[^\n\r\"]+            {string.append(yytext());}
\\t                   {string.append("\t");}
\\n                   {string.append("\n");}
\\r                   {string.append("\r");}
\\\"                  {string.append("\"");}
\\                    {string.append("\\");}

}

<CHAR>
{

\'                    {yybegin(YYINITIAL); return symbol(sym.CHAR_CONST, string.toString());}

[^\n\r\']+            {string.append(yytext()); if(string.length() > 1) throw new Error("Too much chars");}


}

<COMMENT>
{

"%"                   {yybegin(YYINITIAL);}
[^%]                  {;}

}

[^]                   {if(yystate() == STRING) handleUnclosedStringError();
                       else throw new Error("Illegal " + yytext() + " at line " + (yyline+1));}




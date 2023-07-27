grammar EL;



expr :
        NUMBER #Number
        | STRING # String
        | TRUE # Boolean
        | FALSE # Boolean
        | '(' expr ')' #Parentheses
        | STD'(' expr ')' # Std
        | AVG'(' expr ')' # Avg
        | VAR'(' expr ')' # Var
        | COUNT'(' expr ')' #Count
        | DISTINCT'(' expr')'#Distinct
        | MIN'(' expr ')' #Min
        | MAX'(' expr ')' #Max
        | SUM'(' expr ')' #Sum
        | ISNULL'(' expr ')' #IsNull
        | NOTNULL'(' expr ')' #NotNull
        | ISEMPTY'(' expr ')' #IsEmpty
        | NOTEMPTY'(' expr ')' #NotEmpty
        | ('$'|'@')ID  # contextField
        | expr MEMBERACCESSOR ID #MemberAccess
        | expr MEMBERACCESSOR ID'(' exprList? ')' #FuncCall
        | <assoc=right> expr POW  expr   #Pow
        | expr (MULT|DIV|MOD) expr #MulDivMod
        | expr (PLUS|SUB) expr #AddSub
        | expr IN expr #In
        | expr (LT|LTEQ|GT|GTEQ|EQUALS|NOTEQUALS) expr #Compare
        | NOT expr #Opposite
        | expr (AND | OR) expr #AndOr
        | expr AS ID #Alias
      ;

exprList : (expr)(',' expr)*;

NUMBER: '-'?DIGIT+('.'DIGIT+)?;


OR    :     '||' | 'or' | 'OR';
AND   :     '&&' | 'and' | 'AND';
EQUALS
      :    '=' | '==';
NOTEQUALS
      :    '!=' | '<>';
LT    :    '<';
LTEQ  :    '<=';
GT    :    '>';
GTEQ  :    '>=';
PLUS  :    '+';
SUB   :    '-';
MULT  :    '*';
DIV   :    '/';
MOD   :    '%';
POW   :    '^';
NOT   :    '!' | 'not' | 'NOT';
IN    :    'in' | 'IN';
AS    :    'AS' | 'As' | 'as';
FALSE : 'false' | 'False' | 'FALSE' ;
TRUE : 'true' | 'True' | 'TRUE';

MEMBERACCESSOR : '.' | '->';

STD : S T D;
AVG : A V G;
VAR : V A R;
MAX : M A X;
MIN : M I N;
SUM : S U M;
COUNT : C O U N T;
DISTINCT : D I S T I N C T;
ISNULL : I S '_'? N U L L;
NOTNULL : N O T '_'? N U L L;
ISEMPTY : I S '_'? E M P T Y;
NOTEMPTY: N O T '_'? E M P T Y;


ID :  ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;
STRING : '"'.*?'"'|'\''.*?'\'';

LINE_COMMENT : '//'.*?'\r'?'\n' -> skip ;
COMMENT : '/*'.*?'*/'-> skip ;
WS : (' '| '\t' | '\r' | '\n')+ -> skip ;

InvalidIdentifier: [0-9]([0-9a-zA-Z])+;
UnknownToken : . ;

fragment
ESC : '\\"' | '\\\\';

fragment
DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
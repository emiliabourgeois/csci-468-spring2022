# Catscript Guide

This document is used as a guide for catscript, to satisfy capstone requirement 4.

## Introduction

Catscript is a simple scripting language.  Here is an example:

~~~
var x = "foo"
print(x)
~~~

## Features

### Types

Catscript has five types and one complex type:
- int - a 32 bit integer
- string - a java-style string
- bool - a boolean value
- list - a list of value with the type 'x'
- null - the null type
- object - any type of value

Variables can be declared with and without types

```
var x = "foo"
var x : string = "foo"
```

Lists are special types which also store a type of the list which can be any of the other five. Declared using:

```var x : list<Type> = [1,2,3]```
### Basic Expressions
- ```+``` addition
- ```-``` subtraction
- ```*``` multiplication
- ```\``` division
- ```not``` not
- ```-``` negate
- ```true```
- ```false```
- ```null```

Examples:
~~~
var q = -1 + 1 * 4
if(not x)
if(x!=null)
else if(false)
~~~
### If & Else statements

If statements can be used alone like so:

~~~
if(x){
    var y = 10
}
~~~

With an else:

~~~
if(x){
    var y = 10
}
else {
    var y = 20
}
~~~

Or with an else if:
~~~
if(x){
    var y = 10
}
else if(z){
    var y = 20
}
~~~

and all can be mixed and matched as long as an if statement is first and if there is an else it is only at the end like so:

~~~
if(x){
    var y = 10
}
else if(z){
    var y = 20
}
else {
    var y = 30
}
~~~
### Equality & Comparison Expressions

Catscript supports basic equality and comparison expressions.

- Less than (<)
- Less than or equal to (<=)
- Greater than (>)
- Greater than or equal to (>=)
- Not equal (!=)
- Equals (==)

They can be used in if and else statements like so (all are true statements):
~~~
if(1<2)
if(1==1)
if(1<=1)
if(2!=1)
~~~
### Print Statements

Any expression can be printed like so:

```print(exp)```

### For loops

For loops can be used to iterate through lists.

~~~
var lst = [1, 2, 3]

for( i in lst ) {
    print(i)
}
~~~
### Functions

Functions are declared with a body like so: 
~~~
function foo(){
    print("hello")
}
~~~

Functions can be called with function call statements:

~~~
foo()
~~~
result:
~~~
hello
~~~

Functions can also be declared with any number of parameters

~~~Jav
function foo(x : int, y : String) {
    print(x)
}
~~~

The function call for this function is:

~~~
foo(10,"hi")
~~~

Optionally functions can pass a value back to the origin of the function call using return statements.

~~~
function foo(x : int, y : String) {
    return x
}
print(foo(10,"hi"))
~~~
## CatScript Grammar
~~~ebnf
catscript_program = { program_statement };

program_statement = statement |
                    function_declaration;

statement = for_statement |
            if_statement |
            print_statement |
            variable_statement |
            assignment_statement |
            function_call_statement;

for_statement = 'for', '(', IDENTIFIER, 'in', expression ')', 
                '{', { statement }, '}';

if_statement = 'if', '(', expression, ')', '{', 
                    { statement }, 
               '}' [ 'else', ( if_statement | '{', { statement }, '}' ) ];

print_statement = 'print', '(', expression, ')'

variable_statement = 'var', IDENTIFIER, 
     [':', type_expression, ] '=', expression;

function_call_statement = function_call;

assignment_statement = IDENTIFIER, '=', expression;

function_declaration = 'function', IDENTIFIER, '(', parameter_list, ')' + 
                       [ ':' + type_expression ], '{',  { function_body_statement },  '}';

function_body_statement = statement |
                          return_statement;

parameter_list = [ parameter, {',' parameter } ];

parameter = IDENTIFIER [ , ':', type_expression ];

return_statement = 'return' [, expression];

expression = equality_expression;

equality_expression = comparison_expression { ("!=" | "==") comparison_expression };

comparison_expression = additive_expression { (">" | ">=" | "<" | "<=" ) additive_expression };

additive_expression = factor_expression { ("+" | "-" ) factor_expression };

factor_expression = unary_expression { ("/" | "*" ) unary_expression };

unary_expression = ( "not" | "-" ) unary_expression | primary_expression;

primary_expression = IDENTIFIER | STRING | INTEGER | "true" | "false" | "null"| 
                     list_literal | function_call | "(", expression, ")"

list_literal = '[', expression,  { ',', expression } ']'; 

function_call = IDENTIFIER, '(', argument_list , ')'

argument_list = [ expression , { ',' , expression } ]

type_expression = 'int' | 'string' | 'bool' | 'object' | 'list' [, '<' , type_expression, '>']
~~~
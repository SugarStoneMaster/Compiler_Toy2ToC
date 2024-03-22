# Context
This project was developed in the context of a master degree course ("Compilers") and translates from a custom language named "Toy2" to C.

## Toy 2
Toy2 is an high level programming language, its tokens and the grammar are specified in the `srcjflexcup` directory.

## Lexer
The lexer is automatically generated with jflex

## Parser
The parser is automatically generated with javacup

## Semantic Analysis and C code generation
Semantic Analysis and C code generation are implemented through different visitors which navigates the syntax tree
### ScopingVisitor
It populates the **type environment** of the entire program (saving it in the "environment" property of BodyNode) and performs **type inference**. It also renames all identifiers defined in the input Toy2 program that conflict with C keywords (for example, "continue", "break", etc.) and/or conflict with identifiers defined personally for utility purposes (for example, a utility function "freeAll" to free memory in C: if the Toy2 program has a function with this name, it will be changed). It also handles **multiple declaration errors**.


### SemanticAnalysisVisitor 
Performs semantic analysis of Toy2 and halts C compilation if errors are detected. Error detection **doesn't stop at the first encountered error** but continues analysis to identify others (if there are more than one): for example, an error will be generated for each use of an undeclared variable, as well as other errors of different types.
### CodeGeneratorVisitor 
It generates C code, properly indented (useful for potential debugging), and with some utility functions: conversion of other types to strings for concatenation, deallocation of string memory. Functions with a single return value become normal functions in C, functions with more than one return value are declared as void, and all return values are passed by reference.
## Test files
In the `test_files` directory, there is the exercise on the calculator and 4 invalid tests.

# Getting started

Maven, Java and GCC are needed to run this project

Compile the project using Maven and then run it giving it a .txt file that contains a program written in Toy2

## An example of Toy2 program
```c
proc main():
    var a, b, result : real; op : string; continue ^= true;\ %la variabile continue verrà rinominata perché è una keyword del C%
    loop(@a, @b, @result, @op, @continue);

endproc



proc loop(out a: real, out b: real, out result: real, out op: string, out continue: boolean):
    var z ^= 2;\
    while continue = true do
        <-- "Che operazione aritmetica vuoi svolgere? (+, -, *, /)    " $(op);
        while op <> "+" && op <> "-" && op <> "*" && op <> "/" do
            <-- "Devi inserire un'operazione aritmetica consentita (+, -, *, /)    " $(op);
        endwhile;

        <-- "inserisci il primo numero    " $(a) "inserisci il secondo numero    " $(b);
        result ^= calculate(a, b, op);
        -->! "Il risultato è " $(result);

        while z < 0 || z > 1 do
            <-- "Vuoi continuare? (0: no, 1: sì)    " $(z);
            if z = 0 then
                continue ^= false;
            endif;
        endwhile;
        z ^= 2;
    endwhile;

endproc


func calculate(a: real, b: real, op: string) -> real:
    var result: real;\

    if op = "+" then
        result ^= a + b;
    elseif op = "-" then
        result ^= a - b;
    elseif op = "*" then
        result ^= a * b;
    elseif op = "/" then
        result ^= a / b;
    endif;

    return result;
endfunc

%
func calculatee(a: real, b: real, op: string, continue: string) -> real, real, real, real, real, real, real, real, real, real:
    var result: real;\
    var cicco: integer;\
    var return25: real;\
    var continuee: string;\
    cicco ^= 3;


    if op = "+" then
        result ^= a + b;
    elseif op = "-" then
        result ^= a - b;
    elseif op = "*" then
        result ^= a * b;
    elseif op = "/" then
        result ^= a / b;
    endif;

    return result, result, result, result, result, result, result, result, result, result;
endfunc
```
## The translation in C
```c
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>

char* convert_and_printF(float);
char* convert_and_printI(int); 
char* convert_and_printB(bool); 

void readInput(char**);

void freeAll();

char *charPointers[1000];
int indexPointer = 0;

size_t len = 0;



void loop(float*, float*, float*, char**, bool*);
float calculate(float, float, char*);

int main()
{
    float a, b, result;
    char* op = NULL;
    charPointers[indexPointer] = op;
    indexPointer++;
    bool continuee = true;
    loop(&a, &b, &result, &op, &continuee);
    freeAll();
    return 0;
}

void loop(float* a, float* b, float* result, char** op, bool* continuee)
{
    int z = 2;
    while(*continuee == true)
    {
        printf("Che operazione aritmetica vuoi svolgere? (+, -, *, /)    ");
        fflush(stdin);
        readInput(op);
        while(strcmp(*op, "+") != 0 && strcmp(*op, "-") != 0 && strcmp(*op, "*") != 0 && strcmp(*op, "/") != 0)
        {
            printf("Devi inserire un'operazione aritmetica consentita (+, -, *, /)    ");
            fflush(stdin);
            readInput(op);
        }
        printf("inserisci il primo numero    ");
        scanf("%f", a);
        printf("inserisci il secondo numero    ");
        scanf("%f", b);
        *result = calculate(*a, *b, *op);
        printf("Il risultato è %f\n", *result);
        while(z < 0 || z > 1)
        {
            printf("Vuoi continuare? (0: no, 1: sì)    ");
            scanf("%d", &z);
            if(z == 0)
            {
                *continuee = false;
            }
        }
        z = 2;
    }
}


float calculate(float a, float b, char* op)
{
    float result;
    if(strcmp(op, "+") == 0)
    {
        result = a + b;
    }
    else if(strcmp(op, "-") == 0)
    {
        result = a - b;
    }
    else if(strcmp(op, "*") == 0)
    {
        result = a * b;
    }
    else if(strcmp(op, "/") == 0)
    {
        result = a / b;
    }
    return result;
}


//utility functions
char* convert_and_printF(float f)
{
    char* str = (char*) malloc(32 * sizeof(char)); 
    sprintf(str, "%.5f", f); 
    charPointers[indexPointer] = str;
    indexPointer++;
    return str;
}

char* convert_and_printI(int i) 
{
     char* str = (char*) malloc(32 * sizeof(char)); 
     sprintf(str, "%d", i); 
     charPointers[indexPointer] = str;
     indexPointer++;
     return str;
}

char* convert_and_printB(bool b) 
{
     char* str = (char*) malloc(8 * sizeof(char)); 
     sprintf(str, "%s", b ? "true" : "false"); 
     charPointers[indexPointer] = str;
     indexPointer++;
     return str;
}

void readInput(char **str)
{
    *str = malloc(1001);
    fgets(*str, 1000, stdin);
    (*str)[strcspn(*str, "\n")] = '\0';
    *str = realloc(*str, strlen(*str) + 3);
    charPointers[indexPointer] = *str;
    indexPointer++;
}void freeAll()
{
     for(int i = 0; i < indexPointer; i++)
         free(charPointers[i]);
}



```






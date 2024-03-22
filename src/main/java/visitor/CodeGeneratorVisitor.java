package visitor;

import nodes.*;
import nodes.statements.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CodeGeneratorVisitor implements Visitor{

    private Environment top;

    private StringBuilder codeBuffer;

    private String currentIndent;



    public CodeGeneratorVisitor(Environment top)
    {
        this.top = top;
        codeBuffer = new StringBuilder();
        currentIndent = "";
    }


    @Override
    public Object visit(ProgramNode node) {
        codeBuffer.append("#include <stdio.h>\n" +
                "#include <string.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <stdbool.h>\n\n" +
                "char* convert_and_printF(float);\n" +
                "char* convert_and_printI(int); \n" +
                "char* convert_and_printB(bool); \n\n");
        codeBuffer.append("void readInput(char**);\n\n");
        codeBuffer.append("void freeAll();\n\n");




        codeBuffer.append("char *charPointers[1000];\n" +
                "int indexPointer = 0;\n\n"); //to keep track of pointers for freeing
        codeBuffer.append("size_t len = 0;\n\n\n"); //for getline in string reading

        for(VarDeclNode varDeclNode : node.varDeclarations)
            varDeclNode.accept(this);
        codeBuffer.append("\n");

        //functions and procedure signs
        for(Map.Entry<String, Record> entry : top.table.entrySet())
        {
            Record record = entry.getValue();
            if(record.kind.equals("function") || (record.kind.equals("procedure") && !(record.name.equals("main"))))
            {
                if(record.kind.equals("procedure") || (record.kind.equals("function") && record.returnTypes.size() > 1))
                    codeBuffer.append("void ");
                else
                    codeBuffer.append(getTypeInC(record.returnTypes.get(0)) + " ");

                codeBuffer.append(record.name + "(");
                for(String type : record.types)
                {
                    codeBuffer.append(getTypeInC(type));
                    codeBuffer.append(", ");
                }
                if(record.returnTypes != null)
                    for(int i = 0; i < record.returnTypes.size() && record.returnTypes.size() > 1; i++)
                    {
                        codeBuffer.append(getTypeInC(record.returnTypes.get(i)));
                        codeBuffer.append("*, ");
                    }

                if(!record.types.isEmpty() || record.returnTypes.size() > 1)
                    codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
                codeBuffer.append(");\n");
            }
        }
        codeBuffer.append("\n");

        ProcedureNode mainProcedure = null;
        for(ProcedureNode procedureNode : node.procedures)
            if(procedureNode.name.equals("main"))
                mainProcedure = procedureNode;

        mainProcedure.accept(this);

        for(ProcedureNode procedureNode : node.procedures)
            if(!(procedureNode.name.equals("main")))
                procedureNode.accept(this);

        for(FunctionNode functionNode : node.functions)
            functionNode.accept(this);

        codeBuffer.append("//utility functions\n");
        codeBuffer.append("char* convert_and_printF(float f)\n" +
                "{\n" +
                "    char* str = (char*) malloc(32 * sizeof(char)); \n" +
                "    sprintf(str, \"%.5f\", f); \n" +
                "    charPointers[indexPointer] = str;\n" +
                "    indexPointer++;\n" +
                "    return str;\n" +
                "}\n" +
                "\n" +
                "char* convert_and_printI(int i) \n" +
                "{\n" +
                "     char* str = (char*) malloc(32 * sizeof(char)); \n" +
                "     sprintf(str, \"%d\", i); \n" +
                "     charPointers[indexPointer] = str;\n" +
                "     indexPointer++;\n" +
                "     return str;\n" +
                "}\n" +
                "\n" +
                "char* convert_and_printB(bool b) \n" +
                "{\n" +
                "     char* str = (char*) malloc(8 * sizeof(char)); \n" +
                "     sprintf(str, \"%s\", b ? \"true\" : \"false\"); \n" +
                "     charPointers[indexPointer] = str;\n" +
                "     indexPointer++;\n" +
                "     return str;\n" +
                "}\n\n");
        codeBuffer.append("void readInput(char **str)\n" +
                "{\n" +
                "    *str = malloc(1001);\n" +
                "    fgets(*str, 1000, stdin);\n" +
                "    (*str)[strcspn(*str, \"\\n\")] = '\\0';\n" +
                "    *str = realloc(*str, strlen(*str) + 3);\n" +
                "    charPointers[indexPointer] = *str;\n" +
                "    indexPointer++;\n" +
                "}");
        codeBuffer.append("void freeAll()\n" +
                "{\n" +
                "     for(int i = 0; i < indexPointer; i++)\n" +
                "         free(charPointers[i]);\n" +
                "}\n\n");

        return "";
    }

    @Override
    public Object visit(VarDeclNode node) {
        for(VarSingleDeclNode varSingleDeclNode : node.declarations)
            varSingleDeclNode.accept(this);

        return "";
    }

    @Override
    public Object visit(VarSingleDeclNode node) {
        if(node.type != null)
        {
            codeBuffer.append(currentIndent + getTypeInC(node.type) + " ");
            for(int i = 0; i < node.identifiers.size(); i++)
            {
                if(node.type.equals("string"))
                {
                    if(i >= 1)
                        codeBuffer.append(currentIndent + getTypeInC(node.type) + " ");
                    node.identifiers.get(i).accept(this);
                    codeBuffer.append(" = NULL;\n");
                    codeBuffer.append(currentIndent + "charPointers[indexPointer] = ");
                    node.identifiers.get(i).accept(this);
                    codeBuffer.append(";\n");
                    codeBuffer.append(currentIndent + "indexPointer++;\n");
                }
                else
                {
                    node.identifiers.get(i).accept(this);
                    codeBuffer.append(", ");
                }

            }
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
            codeBuffer.append(";\n");
        }
        else if(node.initialValues != null)
        {
            for(int i = 0; i < node.identifiers.size(); i++)
            {
                Record found = top.getFromThisTable(node.identifiers.get(i).name);
                codeBuffer.append(currentIndent + getTypeInC(found.type) + " ");
                node.identifiers.get(i).accept(this);
                codeBuffer.append(" = ");
                if(node.initialValues.get(i).value.getClass().getSimpleName().equals("String"))
                    codeBuffer.append("strdup(");
                node.initialValues.get(i).accept(this); //append const
                if(node.initialValues.get(i).value.getClass().getSimpleName().equals("String"))
                {
                    codeBuffer.append(");\n");
                    codeBuffer.append(currentIndent + "charPointers[indexPointer] = ");
                    node.identifiers.get(i).accept(this);
                    codeBuffer.append(";\n");
                    codeBuffer.append(currentIndent + "indexPointer++");
                }
                codeBuffer.append(";\n");
            }
        }

        return "";
    }

    @Override
    public Object visit(FunctionNode node) {
        if(node.returnTypes.size() > 1)
            codeBuffer.append("void ");
        else
            codeBuffer.append(getTypeInC(node.returnTypes.get(0)) + " ");
        codeBuffer.append(node.name + "(");
        for(IdNode idNode : node.parameters)
            codeBuffer.append(getTypeInC(idNode.idType) + " " + node.body.environment.getFromTypeEnvironment(idNode.name).name + ", ");
        for(int i = 0; i < node.returnTypes.size() && node.returnTypes.size() > 1; i++)
            codeBuffer.append(getTypeInC(node.returnTypes.get(i)) + "* " + "return" + (i+1) + ", ");
        if(!node.parameters.isEmpty() || node.returnTypes.size() > 1)
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
        codeBuffer.append(")\n");

        node.body.accept(this); //append body

        codeBuffer.append("\n\n");

        return "";
    }

    @Override
    public Object visit(ProcedureNode node) {
        if(node.name.equals("main"))
            codeBuffer.append("int " + node.name + "(");
        else
            codeBuffer.append("void " + node.name + "(");

        for(IdNode idNode : node.parameters)
            codeBuffer.append(getTypeInC(idNode.idType) + (idNode.isOut ? "* " : " ") + node.body.environment.getFromTypeEnvironment(idNode.name).name + ", ");

        if(!node.parameters.isEmpty())
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma

        codeBuffer.append(")\n");

        node.body.accept(this); //append body

        if(node.name.equals("main"))
        {
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
            codeBuffer.append("    " + "freeAll();\n");
            codeBuffer.append("    " + "return 0;\n");
            codeBuffer.append("}");
        }

        codeBuffer.append("\n\n");

        return "";
    }

    @Override
    public Object visit(AssignStatementNode node) {
        Record foundVar = null;
        int currentId = 0;
        for(int i = 0; i < node.expressions.size(); i++)
        {

            if(!(node.expressions.get(i).operator.equals("funcall")))
            {
                foundVar = top.getFromTypeEnvironment(node.ids.get(currentId).name);
                codeBuffer.append(currentIndent + (foundVar.isOut ? "*" : ""));
                node.ids.get(currentId).accept(this);
                codeBuffer.append(" = ");
                if(node.expressions.get(i).operator.equals("string"))
                    codeBuffer.append("strdup(");
                node.expressions.get(i).accept(this);
                if(node.expressions.get(i).operator.equals("string"))
                    codeBuffer.append(")");
                codeBuffer.append(";\n");
                if(foundVar.type.equals("string") && !(node.expressions.get(i).node1 instanceof IdNode))
                {
                    codeBuffer.append(currentIndent + "charPointers[indexPointer] = ");
                    codeBuffer.append((foundVar.isOut ? "*" : "") + foundVar.name);
                    codeBuffer.append(";\n");
                    codeBuffer.append(currentIndent + "indexPointer++;\n");
                }
                currentId++;
            }
            else
            {
                FunCallNode funCallNode = (FunCallNode) node.expressions.get(i).node1;

                Record foundFunc = top.getFromTypeEnvironment(funCallNode.functionName);
                if(foundFunc.returnTypes.size() == 1)
                {
                    foundVar = top.getFromTypeEnvironment(node.ids.get(currentId).name);
                    codeBuffer.append(currentIndent + (foundVar.isOut ? "*" : ""));
                    node.ids.get(currentId).accept(this);
                    currentId++;
                    codeBuffer.append(" = ");
                }
                codeBuffer.append((foundFunc.returnTypes.size() == 1 ? "" : currentIndent) + funCallNode.functionName + "(");

                for(ExprNode exprNode : funCallNode.arguments)
                {
                    String name = (String) exprNode.accept(this);
                    Record found = top.getFromTypeEnvironment(name);
                    if(found != null)
                        if(found.isOut)
                        {
                            codeBuffer.replace(codeBuffer.length() - name.length(), codeBuffer.length(), ""); //to put * before
                            codeBuffer.append("*" + name);
                        }
                    codeBuffer.append(", ");
                }


                for(int j = 0; j < foundFunc.returnTypes.size() && foundFunc.returnTypes.size() > 1; j++)
                {
                    foundVar = top.getFromTypeEnvironment(node.ids.get(currentId).name);
                    codeBuffer.append((foundVar.isOut ? "" : "&"));
                    node.ids.get(currentId).accept(this);
                    codeBuffer.append(", ");
                    currentId++;
                }

                if(funCallNode.arguments.size() > 0)
                    codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
                codeBuffer.append(");\n");

                if(foundVar.type.equals("string"))
                {
                    codeBuffer.append(currentIndent + "charPointers[indexPointer] = ");
                    codeBuffer.append((foundVar.isOut ? "*" : "") + foundVar.name);
                    codeBuffer.append(";\n");
                    codeBuffer.append(currentIndent + "indexPointer++;\n");
                }
            }
        }
        return "";
    }

    @Override
    public Object visit(IfStatementNode node) {
        codeBuffer.append(currentIndent + "if(");
        node.condition.accept(this); //append condition
        codeBuffer.append(")\n");

        node.thenBody.accept(this);

        for(ElifNode elifNode : node.elifs)
            elifNode.accept(this);

        if(node.elseBody != null)
        {
            codeBuffer.append(currentIndent + "else\n");
            node.elseBody.accept(this);

        }


        return "";
    }

    @Override
    public Object visit(ProcCallNode node) {
        codeBuffer.append(currentIndent + node.procedureName + "(");


        for(ProcArgumentNode procArgumentNode : node.arguments)
            procArgumentNode.accept(this);

        if(!node.arguments.isEmpty())
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma


        codeBuffer.append(");\n");

        return "";
    }

    @Override
    public Object visit(ReadStatementNode node) {
        for(ExprNode exprNode : node.exprs)
        {
            if(!exprNode.isDollar)
            {
                codeBuffer.append(currentIndent + "printf(");
                exprNode.accept(this);
                codeBuffer.append(");\n");
            }
            else
            {
                IdNode idNode = (IdNode) exprNode.node1;
                Record found = top.getFromTypeEnvironment(idNode.name);
                idNode.idType = found.type;
                idNode.isOut = found.isOut;
                if(!(idNode.idType.equals("string")))
                {
                    codeBuffer.append(currentIndent + "scanf(\"" + getTypeInCIO(idNode.idType) + "\", " + (idNode.isOut  ? "" : "&"));
                    idNode.accept(this);
                    codeBuffer.append(");\n");


                }

                else
                {
                    codeBuffer.append(currentIndent + "fflush(stdin);\n");
                    codeBuffer.append(currentIndent + "readInput(" + (idNode.isOut ? "" : "&"));
                    idNode.accept(this);
                    //codeBuffer.append(", &len, stdin");
                    codeBuffer.append(");\n");

                    /*
                    codeBuffer.append(currentIndent);
                    if(idNode.isOut)
                    {
                        codeBuffer.append("(*");
                        idNode.accept(this);
                        codeBuffer.append(")");
                    }
                    else
                        idNode.accept(this);
                    codeBuffer.append("[strcspn(");
                    codeBuffer.append((idNode.isOut ? "*" : ""));
                    idNode.accept(this);
                    codeBuffer.append(", \"\\n\")] = '\\0';\n");*/

                }
                    //codeBuffer.append(currentIndent + "asprintf(" + (idNode.isOut ? "" : "&") + idNode.accept(this) + ", \"%s\"")
            }
        }

        return "";
    }

    @Override
    public Object visit(ReturnStatementNode node) {
        for(int i = 0; i < node.returnExpressions.size() && node.returnExpressions.size() > 1; i++)
        {
            codeBuffer.append(currentIndent + "*return" + (i+1) + " = ");
            node.returnExpressions.get(i).accept(this);
            codeBuffer.append(";\n");
        }

        if(node.returnExpressions.size() == 1)
        {
            codeBuffer.append(currentIndent + "return ");
            if(node.returnExpressions.get(0).nodeType.equals("string"))
            {
                codeBuffer.append("strdup(");
                node.returnExpressions.get(0).accept(this);
                codeBuffer.append(")");
            }
            else
                node.returnExpressions.get(0).accept(this);
            codeBuffer.append(";\n");
        }


        return "";
    }

    @Override
    public Object visit(WhileStatementNode node) {
        codeBuffer.append(currentIndent + "while(");
        node.condition.accept(this);
        codeBuffer.append(")\n");

        node.body.accept(this);

        return "";
    }

    @Override
    public Object visit(ForStatementNode node) {
        codeBuffer.append(currentIndent + "for(");

        node.init.accept(this);
        codeBuffer.replace(codeBuffer.length() - 1, codeBuffer.length(), "");
        codeBuffer.append(" ");

        codeBuffer.append(node.variableName + " <= ");
        node.toInt.accept(this);
        codeBuffer.append("; ");

        codeBuffer.append(node.variableName + " = " + node.variableName + " + ");
        node.step.accept(this);
        codeBuffer.append(")\n");

        node.body.accept(this);

        return "";
    }

    @Override
    public Object visit(WriteStatementNode node) {
        codeBuffer.append(currentIndent + "printf(\"");
        for(ExprNode exprNode : node.expressions)
        {
            if(!exprNode.isDollar)
            {
                if(exprNode.operator.equals("add")) //string concatenation
                    codeBuffer.append("%s");
                else
                {
                    String value = (String) exprNode.accept(this);
                    //remove extra "" at start and end
                    codeBuffer.replace(codeBuffer.length() - 1, codeBuffer.length(), "");
                    codeBuffer.replace(codeBuffer.length() - value.length() -1, codeBuffer.length() - value.length(), "");
                }
            }
            else
                codeBuffer.append(getTypeInCIO(exprNode.nodeType));
        }
        codeBuffer.append((node.newLine ? "\\n" : "") + "\"");

        for(ExprNode exprNode : node.expressions)
        {
            if(exprNode.isDollar)
            {
                codeBuffer.append(", ");
                String value = (String) exprNode.accept(this);
                if(exprNode.node1 instanceof IdNode idNode)
                {
                    String key = idNode.name;
                    Record found = top.getFromTypeEnvironment(key);
                    if(found != null)
                        if(found.isOut)
                        {
                            codeBuffer.replace(codeBuffer.length() - value.length(), codeBuffer.length(), ""); //to put * before
                            codeBuffer.append("*" + value);
                        }
                }

            }
            else if(!exprNode.isDollar && exprNode.operator.equals("add"))
            {
                codeBuffer.append(", ");
                exprNode.accept(this);
            }

        }

        codeBuffer.append(");\n");

        return "";
    }

    @Override
    public Object visit(BodyNode node) {
        top = node.environment;
        String prevIndent = currentIndent;
        currentIndent += "    ";
        codeBuffer.append(prevIndent + "{\n");
        for(Node n : node.nodes)
            n.accept(this);

        codeBuffer.append(prevIndent + "}\n");

        currentIndent = prevIndent;
        top = top.exitScope();

        return "";
    }

    @Override
    public Object visit(ConstNode node) {
        codeBuffer.append((node.value.getClass().getSimpleName().equals("String") ? "\"" : ""));
        codeBuffer.append((node.value.getClass().getSimpleName().equals("Character") ? "'" : ""));
        codeBuffer.append(node.value);
        codeBuffer.append((node.value.getClass().getSimpleName().equals("String") ? "\"" : ""));
        codeBuffer.append((node.value.getClass().getSimpleName().equals("Character") ? "'" : ""));


        return node.value.toString();
    }

    @Override
    public Object visit(ElifNode node) {
        codeBuffer.append(currentIndent + "else if(");
        node.condition.accept(this);
        codeBuffer.append(")\n");

        node.body.accept(this);

        return "";
    }

    @Override
    public Object visit(ExprNode node) {
        if(node.node1 instanceof ConstNode)
            return node.node1.accept(this);

        if(node.node1 instanceof IdNode)
            return node.node1.accept(this);

        if(node.node1 instanceof FunCallNode)
            return node.node1.accept(this);

        if(node.operator.equals("pare"))
        {
            codeBuffer.append("(");
            node.node1.accept(this);
            codeBuffer.append(")");
            return "";
        }

        if(node.operator.equals("uminus"))
        {
            codeBuffer.append("-");
            return node.node1.accept(this);
        }

        if(node.operator.equals("not"))
        {
            codeBuffer.append("!");
            return node.node1.accept(this);
        }

        ExprNode exprNode1 = (ExprNode) node.node1;
        ExprNode exprNode2 = (ExprNode) node.node2;
        boolean isOut1 = false;
        boolean isOut2 = false;
        boolean isConst1 = false;
        boolean isConst2 = false;
        if(exprNode1.node1 instanceof IdNode)
        {
            Record found = top.getFromTypeEnvironment(((IdNode) exprNode1.node1).name);
            if(found.isOut)
                isOut1 = true;
        }
        if(exprNode2.node1 instanceof IdNode)
        {
            Record found = top.getFromTypeEnvironment(((IdNode) exprNode2.node1).name);
            if(found.isOut)
                isOut2 = true;
        }

        if(exprNode1.node1 instanceof ConstNode)
            isConst1 = true;
        if(exprNode2.node1 instanceof ConstNode)
            isConst2 = true;


        if(exprNode1.nodeType.equals("string") && exprNode2.nodeType.equals("string") && (node.operator.equals("eq") || node.operator.equals("ne") || node.operator.equals("add")))
        {
            if(node.operator.equals("add"))
            {
                codeBuffer.append("strcat(");

                if(isOut1) codeBuffer.append("*");
                else if(isConst1)
                    codeBuffer.append("strdup(");
                exprNode1.accept(this);
                if(!isOut1 && isConst1)
                    codeBuffer.append(")");
                codeBuffer.append(", ");
                if(isOut2) codeBuffer.append("*");
                exprNode2.accept(this);

                codeBuffer.append(")");
            }
            else
            {
                codeBuffer.append("strcmp(");

                if(isOut1) codeBuffer.append("*");
                exprNode1.accept(this);
                codeBuffer.append(", ");
                if(isOut2) codeBuffer.append("*");
                exprNode2.accept(this);

                codeBuffer.append(")");

                if(node.operator.equals("eq")) codeBuffer.append(" == 0");
                else if(node.operator.equals("ne")) codeBuffer.append(" != 0");
            }

            return "";
        }

        if( node.operator.equals("add") && ((exprNode1.nodeType.equals("string") && (exprNode2.nodeType.equals("integer") || exprNode2.nodeType.equals("real") || exprNode2.nodeType.equals("boolean")))
         || (exprNode2.nodeType.equals("string") && (exprNode1.nodeType.equals("integer") || exprNode1.nodeType.equals("real") || exprNode1.nodeType.equals("boolean")))))
        {
            codeBuffer.append("strcat(");

            if(exprNode1.nodeType.equals("string"))
            {
                if(isOut1) codeBuffer.append("*");
                else if(isConst1)
                    codeBuffer.append("strdup(");
                exprNode1.accept(this);
                if(!isOut1 && isConst1)
                    codeBuffer.append(")");
                codeBuffer.append(", ");

                if(exprNode2.nodeType.equals("integer"))
                {
                    codeBuffer.append("convert_and_printI(");
                    if(isOut2) codeBuffer.append("*");
                    exprNode2.accept(this);
                    codeBuffer.append(")");
                }

                if(exprNode2.nodeType.equals("real"))
                {
                    codeBuffer.append("convert_and_printF(");
                    if(isOut2) codeBuffer.append("*");
                    exprNode2.accept(this);
                    codeBuffer.append(")");
                }

                if(exprNode2.nodeType.equals("boolean"))
                {
                    codeBuffer.append("convert_and_printB(");
                    if(isOut2) codeBuffer.append("*");
                    exprNode2.accept(this);
                    codeBuffer.append(")");
                }
            }

            if(exprNode2.nodeType.equals("string"))
            {
                if(exprNode1.nodeType.equals("integer"))
                {
                    codeBuffer.append("convert_and_printI(");
                    if(isOut1) codeBuffer.append("*");
                    exprNode1.accept(this);
                    codeBuffer.append(")");
                }
                else if(exprNode1.nodeType.equals("real"))
                {
                    codeBuffer.append("convert_and_printF(");
                    if(isOut1) codeBuffer.append("*");
                    exprNode1.accept(this);
                    codeBuffer.append(")");
                }
                else if(exprNode1.nodeType.equals("boolean"))
                {
                    codeBuffer.append("convert_and_printB(");
                    if(isOut1) codeBuffer.append("*");
                    exprNode1.accept(this);
                    codeBuffer.append(")");
                }

                codeBuffer.append(", ");

                if(isOut2) codeBuffer.append("*");
                else if(isConst2)
                    codeBuffer.append("strdup(");
                exprNode2.accept(this);
                if(!isOut2 && isConst2)
                    codeBuffer.append(")");
                codeBuffer.append(", ");
            }

            codeBuffer.append(")");

            return "";
        }

        if(isOut1) codeBuffer.append("*");
        node.node1.accept(this);
        switch(node.operator) {
            case "add":
                if(!exprNode1.nodeType.equals("string") && !exprNode2.nodeType.equals("string"))
                    codeBuffer.append(" + ");
                break;

            case "sub":
                codeBuffer.append(" - ");
                break;

            case "times":
                codeBuffer.append(" * ");
                break;

            case "div":
                codeBuffer.append(" / ");
                break;

            case "and":
                codeBuffer.append(" && ");
                break;

            case "or":
                codeBuffer.append(" || ");
                break;

            case "gt":
                codeBuffer.append(" > ");
                break;

            case "ge":
                codeBuffer.append(" >= ");
                break;

            case "lt":
                codeBuffer.append(" < ");
                break;

            case "le":
                codeBuffer.append(" <= ");
                break;

            case "eq":
                codeBuffer.append(" == ");
                break;

            case "ne":
                codeBuffer.append(" != ");
                break;
        }
        if(isOut2) codeBuffer.append("*");
        node.node2.accept(this);

        return "";
    }

    @Override
    public Object visit(FunCallNode node) {
        codeBuffer.append(node.functionName + "(");


        for(ExprNode exprNode : node.arguments)
        {
            exprNode.accept(this);
            codeBuffer.append(", ");
        }

        if(!node.arguments.isEmpty())
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma


        codeBuffer.append(")");

        return "";
    }

    @Override
    public Object visit(IdNode node) {
        Record found = top.getFromTypeEnvironment(node.name);
        codeBuffer.append(found.name);

        return found.name;
    }

    @Override
    public Object visit(ProcArgumentNode node) {
        if(node.variableReferenced != null)
            codeBuffer.append("&" + top.getFromTypeEnvironment(node.variableReferenced).name + ", ");
        else if(node.exprNode != null)
        {
            node.exprNode.accept(this);
            codeBuffer.append(", ");
        }

        return "";
    }


    private String getTypeInC(String type)
    {
        boolean out = type.contains("out");
        if (!out) {
            if (type.contains("integer")) {
                return "int";
            } else if (type.contains("bool")) {
                return "bool";
            } else if (type.contains("real")) {
                return "float";
            } else if (type.contains("string")) {
                return "char*";
            }
             else if(type.contains("char"))
            {
                return "char";
            }
        } else {
            if (type.contains("integer")) {
                return "int*";
            } else if (type.contains("bool")) {
                return "bool*";
            } else if (type.contains("real")) {
                return "float*";
            } else if (type.contains("string")) {
                return "char**";
            } else if (type.contains("char"))
            {
                return "char*";
            }
        }
        return null;
    }

    private String getTypeInCIO(String type)
    {
        if (type.contains("integer")) {
            return "%d";
        } else if (type.contains("bool")) {
            return "%d";
        } else if (type.contains("real")) {
            return "%f";
        } else if (type.contains("string")) {
            return "%s";
        }
        else if (type.contains("char"))
        {
            return "%c";
        }

        return null;
    }

    public String saveCFile(String filePath) {
        String[] filePathSplit = filePath.split("/");
        String fileNameWithExt = filePathSplit[filePathSplit.length - 1];
        String fileName = fileNameWithExt.split("\\.")[0]; //TODO not works with filenames with dots in it

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("test_files" + File.separator + "c_out" + File.separator + fileName + ".c");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        PrintStream output = new PrintStream(fileOutputStream);
        output.println(codeBuffer.toString());

        return codeBuffer.toString();
    }

    public String getC()
    {
        return this.codeBuffer.toString();
    }
}

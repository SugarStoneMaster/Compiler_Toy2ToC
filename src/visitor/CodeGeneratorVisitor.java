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



    //TODO change variable names named "return1,2,3,etc." and FREE MEMORY
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
                "#include <stdbool.h>\n\n");

        codeBuffer.append("size_t len = 0;\n\n"); //for getline in string reading

        for(VarDeclNode varDeclNode : node.varDeclarations)
            varDeclNode.accept(this);

        //functions and procedure signs
        for(Map.Entry<String, Record> entry : top.table.entrySet())
        {
            Record record = entry.getValue();
            if(record.kind.equals("function") || (record.kind.equals("procedure") && !(record.name.equals("main"))))
            {
                //return type
                codeBuffer.append("void ");

                codeBuffer.append(record.name + "(");
                for(String type : record.types)
                {
                    codeBuffer.append(getTypeInC(type));
                    codeBuffer.append(", ");
                }
                if(record.returnTypes != null)
                    for(String type : record.returnTypes)
                    {
                        codeBuffer.append(getTypeInC(type));
                        codeBuffer.append("*, ");
                    }

                if(!record.types.isEmpty() || !record.returnTypes.isEmpty())
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
                        codeBuffer.append("*");
                    node.identifiers.get(i).accept(this);
                    codeBuffer.append(" = NULL");
                    codeBuffer.append(", ");
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
                    codeBuffer.append(")");
                codeBuffer.append(";\n");
            }
        }

        return "";
    }

    @Override
    public Object visit(FunctionNode node) {
        codeBuffer.append("void " + node.name + "(");
        if(node.parameters != null)
            for(IdNode idNode : node.parameters)
                codeBuffer.append(getTypeInC(idNode.idType) + " " + node.body.environment.getFromTypeEnvironment(idNode.name).name + ", ");
        for(int i = 0; i < node.returnTypes.size(); i++)
            codeBuffer.append(getTypeInC(node.returnTypes.get(i)) + "* " + "return" + (i+1) + ", "); //TODO modify lexer or rename variable during scoping
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
        if(node.parameters != null)
        {
            for(IdNode idNode : node.parameters)
                codeBuffer.append(getTypeInC(idNode.idType) + (idNode.isOut ? "* " : " ") + node.body.environment.getFromTypeEnvironment(idNode.name).name + ", ");

            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
        }
        codeBuffer.append(")\n");

        node.body.accept(this); //append body

        if(node.name.equals("main"))
        {
            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
            codeBuffer.append("    " + "return 0;\n");
            codeBuffer.append("}");
        }

        codeBuffer.append("\n\n");

        return "";
    }

    @Override
    public Object visit(AssignStatementNode node) {
        int currentId = 0;
        for(int i = 0; i < node.expressions.size(); i++)
        {
            if(!(node.expressions.get(i).operator.equals("funcall")))
            {
                Record foundVar = top.getFromTypeEnvironment(node.ids.get(currentId).name);
                codeBuffer.append(currentIndent + (foundVar.isOut ? "*" : ""));
                node.ids.get(currentId).accept(this);
                codeBuffer.append(" = ");
                if(node.expressions.get(i).operator.equals("string"))
                    codeBuffer.append("strdup(");
                node.expressions.get(i).accept(this);
                if(node.expressions.get(i).operator.equals("string"))
                    codeBuffer.append(")");
                codeBuffer.append(";\n");
                currentId++;
            }
            else
            {
                FunCallNode funCallNode = (FunCallNode) node.expressions.get(i).node1;
                Record foundFunc = top.getFromTypeEnvironment(funCallNode.functionName);
                codeBuffer.append(currentIndent + funCallNode.functionName + "(");
                if(funCallNode.arguments != null)
                {
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
                }

                for(String returnType : foundFunc.returnTypes)
                {
                    Record foundVar = top.getFromTypeEnvironment(node.ids.get(currentId).name);
                    codeBuffer.append((foundVar.isOut ? "" : "&"));
                    node.ids.get(currentId).accept(this);
                    codeBuffer.append(", ");
                    currentId++;
                }

                codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
                codeBuffer.append(");\n");
            }
        }
        return "";
    }

    @Override //TODO check correctness of else if and else
    public Object visit(IfStatementNode node) {
        codeBuffer.append(currentIndent + "if(");
        node.condition.accept(this); //append condition
        codeBuffer.append(")\n");

        node.thenBody.accept(this);

        if(node.elifs != null)
            for(ElifNode elifNode : node.elifs)
                elifNode.accept(this);

        if(node.elseBody != null)
            node.elseBody.accept(this);


        return "";
    }

    @Override
    public Object visit(ProcCallNode node) {
        codeBuffer.append(currentIndent + node.procedureName + "(");

        if(node.arguments != null)
        {
            for(ProcArgumentNode procArgumentNode : node.arguments)
                procArgumentNode.accept(this);

            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
        }

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
                    codeBuffer.append(currentIndent + "getline(" + (idNode.isOut ? "" : "&"));
                    idNode.accept(this);
                    codeBuffer.append(", &len, stdin");
                    codeBuffer.append(");\n");

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
                    codeBuffer.append(", \"\\n\")] = '\\0';\n");

                }
                    //codeBuffer.append(currentIndent + "asprintf(" + (idNode.isOut ? "" : "&") + idNode.accept(this) + ", \"%s\"")
            }
        }

        return "";
    }

    @Override
    public Object visit(ReturnStatementNode node) {
        for(int i = 0; i < node.returnExpressions.size(); i++)
        {
            codeBuffer.append(currentIndent + "*return" + (i+1) + " = ");
            node.returnExpressions.get(i).accept(this);
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

    @Override //TODO FREE MEMORY?
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
        codeBuffer.append((node.value.getClass().getSimpleName().equals("String") ? "\"" : "") + node.value + (node.value.getClass().getSimpleName().equals("String") ? "\"" : ""));

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

        if(exprNode1.nodeType.equals("string") && exprNode2.nodeType.equals("string") && (node.operator.equals("eq") || node.operator.equals("ne") || node.operator.equals("add")))
        {
            if(node.operator.equals("add"))
            {
                codeBuffer.append("strcat(");

                if(isOut1) codeBuffer.append("*");
                else
                    codeBuffer.append("strdup(");
                exprNode1.accept(this);
                if(!isOut1)
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

        if(isOut1) codeBuffer.append("*");
        node.node1.accept(this);
        switch(node.operator) {
            case "add":
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
        codeBuffer.append(currentIndent + node.functionName + "(");

        if(node.arguments != null)
        {
            for(ExprNode exprNode : node.arguments)
                exprNode.accept(this);

            codeBuffer.replace(codeBuffer.length() -2, codeBuffer.length(), ""); //remove extra comma
        }

        codeBuffer.append(");\n");

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
        } else {
            if (type.contains("integer")) {
                return "int*";
            } else if (type.contains("bool")) {
                return "bool*";
            } else if (type.contains("real")) {
                return "float*";
            } else if (type.contains("string")) {
                return "char**";
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
}

package visitor;

import nodes.*;
import nodes.statements.*;

import java.util.*;

/**
 * Populates the type environment in order to achieve usages of variables before declarations
 *
 * Also achieves type inference
 */
public class ScopingVisitor implements Visitor{
    private Environment top;

    public HashSet<String> allSymbols;

    public ArrayList<String> langKeywords; //in this case lang is C

    public int maxReturns = 1;

    public ScopingVisitor() {
        top = new Environment(null); //entering global scope
        top.table.name = "Globals";
        allSymbols = new HashSet<>();
        langKeywords = new ArrayList<>(Arrays.asList("charPointers", "indexPointer", "freeAll", "len", "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"));
    }

    @Override
    public Object visit(ProgramNode node) {
        for(FunctionNode functionNode : node.functions)
            if(functionNode.returnTypes.size() > maxReturns)
                maxReturns = functionNode.returnTypes.size();
        for(int i = 1; i <= maxReturns; i++)
            langKeywords.add("return" + i);


        for(VarDeclNode varDeclNode : node.varDeclarations)
            varDeclNode.accept(this);

        for(ProcedureNode procedureNode : node.procedures)
            procedureNode.accept(this);

        for(FunctionNode functionNode : node.functions)
            functionNode.accept(this);




        return top;
    }

    @Override
    public Object visit(VarDeclNode node) {
        for(VarSingleDeclNode varSingleDeclNode : node.declarations)
            varSingleDeclNode.accept(this);

        return top; //only for debug and printing
    }

    @Override
    public Object visit(VarSingleDeclNode node) {
        for(int i = 0; i < node.identifiers.size(); i++)
        {
            IdNode idNode = node.identifiers.get(i);
            if(node.type != null)
                top.addId(idNode.name, "variable", node.type, idNode.isOut, idNode.isFuncParam);
            else if (node.initialValues != null && node.initialValues.size() == node.identifiers.size())
            {
                ConstNode constNode = node.initialValues.get(i);
                String type = constNode.value.getClass().getSimpleName().toLowerCase();
                if(type.equals("float")) type = "real";
                if(type.equals("character")) type = "char";

                top.addId(idNode.name, "variable", type, idNode.isOut, idNode.isFuncParam);
            }
        }

        return null;
    }

    @Override
    public Object visit(FunctionNode node) {
        ArrayList<String> types = new ArrayList<>();
        ArrayList<String> returnTypes =  (ArrayList<String>) node.returnTypes;
        for(IdNode idNode : node.parameters)
            types.add(idNode.idType);

        top.addId(node.name, "function", types, returnTypes);

        top = (Environment) node.body.accept(this); //enter scope
        top.table.name = node.name;
        for(IdNode idNode : node.parameters)
            top.addId(idNode.name, "variable", idNode.idType, idNode.isOut, idNode.isFuncParam);

        allSymbols.addAll(top.getAllSymbolsFromTypeEnvironment());
        top.removeLangKeywords(allSymbols, langKeywords);
        node.body.environment = top; //saving current environment

        top = top.exitScope();

        return node.body.environment; //only for debug and printing
    }

    @Override
    public Object visit(ProcedureNode node) {
        ArrayList<String> types = new ArrayList<>();
        for(IdNode idNode : node.parameters)
            types.add(idNode.isOut ? "out " + idNode.idType : idNode.idType);

        top.addId(node.name, "procedure", types);

        top = (Environment) node.body.accept(this); //enter scope
        top.table.name = node.name;
        for(IdNode idNode : node.parameters)
            top.addId(idNode.name, "variable", idNode.idType, idNode.isOut, idNode.isFuncParam);

        allSymbols.addAll(top.getAllSymbolsFromTypeEnvironment());
        top.removeLangKeywords(allSymbols, langKeywords);
        node.body.environment = top; //saving current environment

        top = top.exitScope();

        return node.body.environment; //only for debug and printing
    }

    @Override
    public Object visit(AssignStatementNode node) {
        return null;
    }

    @Override
    public Object visit(IfStatementNode node) {
        top = (Environment) node.thenBody.accept(this); //enter scope
        top.table.name = "then";
        node.thenBody.environment = top;
        top = top.exitScope();


        for(ElifNode elifNode : node.elifs)
            elifNode.accept(this);


        if(node.elseBody != null)
        {
            top = (Environment) node.elseBody.accept(this); //enter scope
            top.table.name = "else";
            node.elseBody.environment = top;
            top = top.exitScope();
        }

        return null;
    }

    @Override
    public Object visit(ProcCallNode node) {
        return null;
    }

    @Override
    public Object visit(ReadStatementNode node) {
        return null;
    }

    @Override
    public Object visit(ReturnStatementNode node) {
        return null;
    }

    @Override
    public Object visit(WhileStatementNode node) {
        top = (Environment) node.body.accept(this); //enter scope
        top.table.name = "while";
        node.body.environment = top;
        top = top.exitScope();

        return null;
    }

    @Override
    public Object visit(ForStatementNode node) {
        top = (Environment) node.body.accept(this); //enter scope
        top.table.name = "for";
        node.body.environment = top;
        top = top.exitScope();

        return null;
    }

    @Override
    public Object visit(WriteStatementNode node) {
        return null;
    }

    @Override
    public Object visit(BodyNode node) {
        top = top.createAndEnterScope();
        for(Node n : node.nodes)
            n.accept(this);

        allSymbols.addAll(top.getAllSymbolsFromTypeEnvironment());
        top.removeLangKeywords(allSymbols, langKeywords);
        return top; //needed for saving environments in bodies
    }

    @Override
    public Object visit(ConstNode node) {
        return null;
    }

    @Override
    public Object visit(ElifNode node) {
        top = (Environment) node.body.accept(this); //enter scope
        top.table.name = "elif";
        node.body.environment = top;
        top = top.exitScope();

        return null;
    }

    @Override
    public Object visit(ExprNode node) {
        return null;
    }

    @Override
    public Object visit(FunCallNode node) {
        return null;
    }

    @Override
    public Object visit(IdNode node) {
        return null;
    }

    @Override
    public Object visit(ProcArgumentNode node) {
        return null;
    }
}

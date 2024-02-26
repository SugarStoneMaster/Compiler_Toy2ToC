package visitor;

import nodes.*;
import nodes.statements.*;

import java.util.ArrayList;
import java.util.Hashtable;

public class ScopingVisitor implements Visitor{
    private Environment top;

    public ScopingVisitor() {
        top = new Environment(null); //entering global scope
    }

    @Override
    public Object visit(ProgramNode node) {
        for(VarDeclNode varDeclNode : node.varDeclarations)
            varDeclNode.accept(this);

        for(ProcedureNode procedureNode : node.procedures)
            procedureNode.accept(this);

        for(FunctionNode functionNode : node.functions)
            functionNode.accept(this);

        return top; //only for debug and printing
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
                top.addId(idNode.name, "variable", node.type, idNode.isOut);
            else if (node.initialValues != null)
            {
                ConstNode constNode = node.initialValues.get(i);
                String type = constNode.value.getClass().getSimpleName().toLowerCase();
                if(type.equals("float")) type = "real";

                top.addId(idNode.name, "variable", type, idNode.isOut);
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
        for(IdNode idNode : node.parameters)
            top.addId(idNode.name, "variable", idNode.idType, idNode.isOut);

        node.body.environment = top; //saving current environment

        top = top.exitScope();

        return node.body.environment; //only for debug and printing
    }

    @Override
    public Object visit(ProcedureNode node) {
        ArrayList<String> types = new ArrayList<>();
        if(node.parameters != null)
            for(IdNode idNode : node.parameters)
                types.add(idNode.isOut ? "out " + idNode.idType : idNode.idType);

        top.addId(node.name, "procedure", types);

        top = (Environment) node.body.accept(this); //enter scope
        if(node.parameters != null)
            for(IdNode idNode : node.parameters)
                top.addId(idNode.name, "variable", idNode.idType, idNode.isOut);

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
        node.thenBody.environment = top;
        top = top.exitScope();

        if(node.elifs != null)
        {
            for(ElifNode elifNode : node.elifs)
                elifNode.accept(this);
        }

        if(node.elseBody != null)
        {
            top = (Environment) node.elseBody.accept(this); //enter scope
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
        top = top.enterScope();
        for(Node n : node.nodes)
            n.accept(this);

        return top; //needed for saving environments in bodies
    }

    @Override
    public Object visit(ConstNode node) {
        return null;
    }

    @Override
    public Object visit(ElifNode node) {
        top = (Environment) node.body.accept(this); //enter scope
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

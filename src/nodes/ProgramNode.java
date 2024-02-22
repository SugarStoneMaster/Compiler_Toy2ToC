package nodes;

import visitor.Visitor;

import java.util.List;

public class ProgramNode extends Node {
    public List<VarDeclNode> varDeclarations;
    public List<FunctionNode> functions;
    public List<ProcedureNode> procedures;

    public ProgramNode(List<VarDeclNode> varDeclarations, List<FunctionNode> functions, List<ProcedureNode> procedures) {
        this.varDeclarations = varDeclarations;
        this.functions = functions;
        this.procedures = procedures;
    }



    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ProgramNode{" +
                "varDeclarations=" + varDeclarations +
                ", functions=" + functions +
                ", procedures=" + procedures +
                '}';
    }
}


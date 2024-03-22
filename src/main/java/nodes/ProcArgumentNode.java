package nodes;

import visitor.Visitor;

public class ProcArgumentNode extends Node{

    public ExprNode exprNode;

    public String variableReferenced;

    public ProcArgumentNode(ExprNode exprNode) {
        this.exprNode = exprNode;
    }

    public ProcArgumentNode(String variableReferenced) {
        this.variableReferenced = variableReferenced;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

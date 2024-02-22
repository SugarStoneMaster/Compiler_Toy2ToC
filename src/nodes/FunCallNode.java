package nodes;

import visitor.Visitor;

import java.util.List;

public class FunCallNode extends Node {
    public String functionName;
    public List<ExprNode> arguments;


    public FunCallNode(String functionName, List<ExprNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

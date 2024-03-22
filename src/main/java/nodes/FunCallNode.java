package nodes;

import visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FunCallNode extends Node {
    public String functionName;
    public List<ExprNode> arguments;


    public FunCallNode(String functionName, List<ExprNode> arguments) {
        this.functionName = functionName;
        this.arguments = Objects.requireNonNullElseGet(arguments, ArrayList::new);
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

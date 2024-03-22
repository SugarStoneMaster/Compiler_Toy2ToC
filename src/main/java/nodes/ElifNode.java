package nodes;

import nodes.Node;
import visitor.Visitor;

public class ElifNode extends Node {
    public ExprNode condition;

    public BodyNode body;

    public ElifNode(ExprNode condition, BodyNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

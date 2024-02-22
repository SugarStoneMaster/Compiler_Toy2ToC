package nodes;

import nodes.Node;
import visitor.Visitor;

public class ElifNode extends Node {
    public ExprNode condition;

    public BodyNode bodyNode;

    public ElifNode(ExprNode condition, BodyNode bodyNode) {
        this.condition = condition;
        this.bodyNode = bodyNode;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

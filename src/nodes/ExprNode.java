package nodes;

import visitor.Visitor;

public class ExprNode extends Node{
    public String operator;

    public Node node1;

    public Node node2;

    public ExprNode(String operator, Node node1) {
        this.operator = operator;
        this.node1 = node1;
    }

    public ExprNode(String operator, Node node1, Node node2) {
        this.operator = operator;
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

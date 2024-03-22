package nodes;

import nodes.statements.StatementNode;
import visitor.Environment;
import visitor.Visitor;

import java.util.List;

public class BodyNode extends Node {

    public List<Node> nodes;

    public Environment environment;
    public BodyNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}


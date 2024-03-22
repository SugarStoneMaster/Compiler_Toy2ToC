package nodes;

import visitor.Environment;
import visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcedureNode extends Node {
    public String name;
    public List<IdNode> parameters;
    public BodyNode body;

    public ProcedureNode(String name, List<IdNode> parameters, BodyNode body) {
        this.name = name;
        this.parameters = Objects.requireNonNullElseGet(parameters, ArrayList::new);
        this.body = body;
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ProcedureNode{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", body=" + body +
                '}';
    }
}

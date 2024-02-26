package nodes;

import visitor.Environment;
import visitor.Visitor;

import java.util.List;

public class FunctionNode extends Node {
    public String name;
    public List<IdNode> parameters;
    public List<String> returnTypes;
    public BodyNode body;

    public FunctionNode(String name, List<IdNode> parameters, List<String> returnTypes, BodyNode body) {
        this.name = name;
        this.parameters = parameters;
        this.returnTypes = returnTypes;
        this.body = body;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FunctionNode{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", returnTypes=" + returnTypes +
                ", body=" + body +
                '}';
    }
}

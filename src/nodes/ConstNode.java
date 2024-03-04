package nodes;

import visitor.Visitor;

public class ConstNode extends Node {
    public Object value; // Could be integer, real, string, boolean

    public ConstNode(Object value) {
        this.value = value;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ConstNode{" +
                "value=" + value +
                '}';
    }
}


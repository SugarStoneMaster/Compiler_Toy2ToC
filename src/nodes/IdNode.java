package nodes;

import visitor.Visitor;

public class IdNode extends Node {
    public String name;
    public String idType;
    public boolean isOut; //if it is passed as reference


    public IdNode(String name, boolean isOut) {
        this.name = name;
        this.isOut = isOut;
    }

    public IdNode(String name) {
        this.name = name;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}



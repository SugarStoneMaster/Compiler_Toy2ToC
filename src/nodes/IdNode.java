package nodes;

import visitor.Visitor;

public class IdNode extends Node {
    public String name;
    public String idType;
    public boolean isOut; //if it is passed as reference

    public boolean isFuncParam; //for semantic to check read-only property for function parameters


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

    @Override
    public String toString() {
        return "IdNode{" +
                "name='" + name + '\'' +
                ", idType='" + idType + '\'' +
                ", isOut=" + isOut +
                ", isFuncParam=" + isFuncParam +
                '}';
    }
}



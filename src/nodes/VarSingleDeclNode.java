package nodes;

import visitor.Visitor;

import java.util.List;

public class VarSingleDeclNode extends Node {
    public List<IdNode> identifiers;
    public String type; // could be null for initializations without a type declaration
    public List<ConstNode> initialValues; // null if no initialization

    public VarSingleDeclNode(List<IdNode> identifiers, String type, List<ConstNode> initialValues) {
        this.identifiers = identifiers;
        this.type = type;
        this.initialValues = initialValues;
    }



    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

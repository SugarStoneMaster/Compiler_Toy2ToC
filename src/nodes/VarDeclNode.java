package nodes;

import visitor.Visitor;

import java.util.List;

public class VarDeclNode extends Node {
    public List<VarSingleDeclNode> declarations;

    public VarDeclNode(List<VarSingleDeclNode> declarations) {
        this.declarations = declarations;
    }



    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

package nodes;

import visitor.Visitor;

public abstract class Node {

    public String typeNode;
    public abstract Object accept(Visitor visitor);
}

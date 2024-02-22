package nodes;

import visitor.Visitor;

public abstract class Node {
    public abstract Object accept(Visitor visitor);
}

package nodes.statements;

import nodes.ProcArgumentNode;
import visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcCallNode extends StatementNode {
    public String procedureName;
    public List<ProcArgumentNode> arguments;

    public ProcCallNode(String procedureName, List<ProcArgumentNode> arguments) {
        this.procedureName = procedureName;
        this.arguments = Objects.requireNonNullElseGet(arguments, ArrayList::new);
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

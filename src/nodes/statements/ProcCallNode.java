package nodes.statements;

import nodes.ProcArgumentNode;
import visitor.Visitor;

import java.util.List;

public class ProcCallNode extends StatementNode {
    public String procedureName;
    public List<ProcArgumentNode> arguments;

    public ProcCallNode(String procedureName, List<ProcArgumentNode> arguments) {
        this.procedureName = procedureName;
        this.arguments = arguments;
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

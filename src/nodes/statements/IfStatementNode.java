package nodes.statements;

import nodes.BodyNode;
import nodes.ElifNode;
import nodes.ExprNode;
import visitor.Visitor;

import java.util.List;

public class IfStatementNode extends StatementNode {
    public ExprNode condition;
    public BodyNode thenBody;
    public List<ElifNode> elifs; // optional
    public BodyNode elseBody; // optional

    public IfStatementNode(ExprNode condition, BodyNode thenBody, List<ElifNode> elifs, BodyNode elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elifs = elifs;
        this.elseBody = elseBody;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

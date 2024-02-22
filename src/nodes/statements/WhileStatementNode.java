package nodes.statements;

import nodes.BodyNode;
import nodes.ExprNode;
import visitor.Visitor;

public class WhileStatementNode extends StatementNode{
    public ExprNode condition;
    public BodyNode body;

    public WhileStatementNode(ExprNode condition, BodyNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

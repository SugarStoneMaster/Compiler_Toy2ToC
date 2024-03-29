package nodes.statements;

import nodes.BodyNode;
import nodes.ElifNode;
import nodes.ExprNode;
import visitor.Environment;
import visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IfStatementNode extends StatementNode {
    public ExprNode condition;
    public BodyNode thenBody;
    public List<ElifNode> elifs; // optional
    public BodyNode elseBody; // optional

    public IfStatementNode(ExprNode condition, BodyNode thenBody, List<ElifNode> elifs, BodyNode elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elifs = Objects.requireNonNullElseGet(elifs, ArrayList::new);
        this.elseBody = elseBody;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

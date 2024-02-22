package nodes.statements;

import nodes.ExprNode;
import nodes.IdNode;
import visitor.Visitor;

import java.beans.Statement;
import java.util.List;

public class AssignStatementNode extends StatementNode {
    public List<IdNode> ids;
    public List<ExprNode> expressions;

    public AssignStatementNode(List<IdNode> ids, List<ExprNode> expressions) {
        this.ids = ids;
        this.expressions = expressions;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

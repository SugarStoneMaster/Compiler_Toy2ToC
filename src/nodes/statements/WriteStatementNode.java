package nodes.statements;

import nodes.ExprNode;
import visitor.Visitor;

import java.util.List;

public class WriteStatementNode extends StatementNode {
    public List<ExprNode> expressions;
    public boolean newLine;

    public WriteStatementNode(List<ExprNode> expressions, boolean newLine) {
        this.expressions = expressions;
        this.newLine = newLine;
    }

    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}


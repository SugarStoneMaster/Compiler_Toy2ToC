package nodes.statements;

import nodes.ExprNode;
import visitor.Visitor;

import java.util.List;

public class ReturnStatementNode extends StatementNode {
    public List<ExprNode> returnExpressions;

    public ReturnStatementNode(List<ExprNode> returnExpressions) {
        this.returnExpressions = returnExpressions;
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}


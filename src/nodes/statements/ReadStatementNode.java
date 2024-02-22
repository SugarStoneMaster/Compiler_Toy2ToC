package nodes.statements;

import nodes.ExprNode;
import visitor.Visitor;

import java.util.List;

public class ReadStatementNode extends StatementNode {
    public List<ExprNode> exprs;

    public ReadStatementNode(List<ExprNode> exprs) {
        this.exprs = exprs;
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}


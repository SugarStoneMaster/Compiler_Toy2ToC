package nodes.statements;

import nodes.BodyNode;
import nodes.ExprNode;
import visitor.Visitor;

public class ForStatementNode extends StatementNode{

    public String variableName;

    public AssignStatementNode init;

    public ExprNode toInt;

    public ExprNode step;

    public BodyNode body;

    public ForStatementNode(String variableName, AssignStatementNode init, ExprNode toInt, ExprNode step, BodyNode body) {
        this.variableName = variableName;
        this.init = init;
        this.toInt = toInt;
        this.step = step;
        this.body = body;
    }


    @Override
    public Object accept(Visitor visitor) {
        return visitor.visit(this);
    }
}

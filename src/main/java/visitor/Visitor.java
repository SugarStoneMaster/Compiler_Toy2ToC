package visitor;

import nodes.*;
import nodes.statements.*;

public interface Visitor {
    Object visit(ProgramNode node);
    Object visit(VarDeclNode node);
    Object visit(VarSingleDeclNode node);

    Object visit(FunctionNode node);
    Object visit(ProcedureNode node);
    //Object visit(StatementNode node);
    Object visit(AssignStatementNode node);
    Object visit(IfStatementNode node);
    Object visit(ProcCallNode node);
    Object visit(ReadStatementNode node);
    Object visit(ReturnStatementNode node);
    Object visit(WhileStatementNode node);
    Object visit(WriteStatementNode node);
    Object visit(BodyNode node);
    Object visit(ConstNode node);
    Object visit(ElifNode node);
    Object visit(ExprNode node);
    Object visit(FunCallNode node);
    Object visit(IdNode node);
    Object visit(ProcArgumentNode node);

    Object visit(ForStatementNode node);


}

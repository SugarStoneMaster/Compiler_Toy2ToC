package visitor;

import nodes.*;
import nodes.statements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SemanticAnalysisVisitor implements Visitor{

    private Environment top;

    public ArrayList<Error> errors;

    public OpTypeTable opTypeTable;

    public SemanticAnalysisVisitor(Environment top)
    {
        this.top = top;
        this.errors = new ArrayList<>();
        this.opTypeTable = new OpTypeTable();
    }

    @Override
    public Object visit(ProgramNode node) {
        String type = "notype";
        try
        {
            Record main = top.getFromThisTable("main");
            if(main == null)
            {
                errors.add(new Error("Main procedure is not declared"));
                type = "error";
            }
            else if(!(main.kind.equals("procedure")))
            {
                errors.add(new Error("Main has to be declared as procedure"));
                type = "error";
            }


            for(VarDeclNode varDeclNode : node.varDeclarations)
                varDeclNode.accept(this);

            for(FunctionNode functionNode : node.functions)
                functionNode.accept(this);

            for(ProcedureNode procedureNode : node.procedures)
                procedureNode.accept(this);

            if(!(errors.isEmpty()))
                throw new Error();
        }catch (Error e)
        {
            for(Error error : errors)
                System.err.println(error.getMessage());

            throw new Error("Compilation aborted");
        }


        return type;
    }

    @Override
    public Object visit(VarDeclNode node) {
        String type = "notype";
        for(VarSingleDeclNode varSingleDeclNode : node.declarations)
        {
            String typeAccept = (String) varSingleDeclNode.accept(this);
            if(typeAccept.equals("error"))
                type = "error";
        }

        return type;
    }

    @Override
    public Object visit(VarSingleDeclNode node) {
        String type = "notype";
        if(node.type != null)
            return type;
        //TODO make error message more clear
        if(node.identifiers.size() != node.initialValues.size())
        {
            errors.add(new Error("Mismatch between number of identifiers and constants"));
            type = "error";
        }

        return type;
    }

    @Override
    public Object visit(FunctionNode node) {
        String type = "notype";
        type = (String) node.body.accept(this); //enter scope previously created

        ReturnStatementNode returnStat = null;
        for(Node n : node.body.nodes)
            if(n.getClass().getSimpleName().equals("ReturnStatementNode"))
                returnStat = (ReturnStatementNode) n;
        if(returnStat == null)
        {
            errors.add(new Error("Return statement not present in function " + node.name));
            type = "error";
        }
        else if (returnStat.returnExpressions.size() != node.returnTypes.size())
        {
            errors.add(new Error("Mismatch between number of return expressions and return types in function " +  node.name));
            type = "error";
        }
        else
        {
            for(int i = 0; i < returnStat.returnExpressions.size(); i++)
            {
                String typeAccept = (String) returnStat.returnExpressions.get(i).accept(this);
                if(!(typeAccept.equals(node.returnTypes.get(i))))
                {
                    errors.add(new Error("Value returned in return statement in function " +  node.name + " are not compatible with return types"));
                    type = "error";
                }
            }
        }




        top = top.exitScope();

        return type;
    }

    @Override
    public Object visit(ProcedureNode node) {
        String type = "notype";
        type = (String) node.body.accept(this); //enter scope previously created

        for(Node n : node.body.nodes)
            if(n.getClass().getSimpleName().equals("ReturnStatementNode"))
            {
                errors.add(new Error("Return statement not permitted in procedure " + node.name));
                type = "error";
            }

        top = top.exitScope();

        return type;
    }

    @Override
    public Object visit(AssignStatementNode node) {
        String type = "notype";
        for(IdNode idNode : node.ids)
        {
            String typeAccept = (String) idNode.accept(this);
            if(typeAccept.equals("error"))
                type = "error";
        }

        for(IdNode idNode : node.ids)
        {
            Record found = top.getFromThisTable(idNode.name);
            if(found != null && found.isFuncParam)
            {
                errors.add(new Error("Function parameter " + idNode.name + " is read-only"));
                type = "error";
            }
        }

        int expressionsSize = 0;
        for(ExprNode exprNode : node.expressions)
        {
            if(exprNode.operator.equals("funcall"))
            {;
                String exprAccept = (String) exprNode.accept(this);
                if(exprAccept.equals("error"))
                    return "error";
                String[] typesArray = exprAccept.split("\\|");
                ArrayList<String> exprTypes = new ArrayList<>(Arrays.asList(typesArray));
                for(String s : exprTypes)
                    expressionsSize++;
            }
            else
                expressionsSize++;
        }


        //TODO make error message more clear
        if(node.ids.size() != expressionsSize)
        {
            errors.add(new Error("Mismatch between identifiers and expressions assigned"));
            type = "error";
        }
        //TODO simplify this
        else
        {
            for (int i = 0, exprIndex = 0; i < node.ids.size() && exprIndex < node.expressions.size(); ) {
                Record found;
                ExprNode exprNode = node.expressions.get(exprIndex);

                String returnType = (String) exprNode.accept(this);
                String[] typesArray = returnType.split("\\|");
                ArrayList<String> exprTypes = new ArrayList<>(Arrays.asList(typesArray));


                for (int typeIndex = 0; typeIndex < exprTypes.size() && i < node.ids.size(); typeIndex++, i++) {
                    String exprType = exprTypes.get(typeIndex);
                    found = top.getFromTypeEnvironment(node.ids.get(i).name);
                    if(found != null)
                    {
                        boolean isCompatibleType = found.type.equals(exprType) || (found.type.equals("real") && exprType.equals("integer")) || (found.type.equals("char") && exprType.equals("integer"));
                        if (!isCompatibleType)
                        {
                            if(!(exprType.equals("error")))
                                errors.add(new Error("Variable " + found.name + " (" + found.type + ") cannot be casted to " + exprType));
                            type = "error";
                        }
                    }
                }

                exprIndex++;
            }
        }

        return type;
    }

    @Override
    public Object visit(IfStatementNode node) {
        String type = "notype";
        String conditionType = (String) node.condition.accept(this);
        if(conditionType == null || !(conditionType.equals("boolean")))
        {
            errors.add(new Error("if condition " + "must be a boolean condition"));
            type = "error";
        }

        type = (String) node.thenBody.accept(this); //enter scope previously created
        top = top.exitScope();

        for(ElifNode elifNode : node.elifs)
        {
            String typeAccept = (String) elifNode.accept(this);
            if(typeAccept.equals("error"))
                type = "error";
        }

        if(node.elseBody != null)
        {
            type = (String) node.elseBody.accept(this);
            top = top.exitScope();
        }


        return type;
    }

    @Override
    public Object visit(ProcCallNode node) {
        String type = "notype";
        Record found = top.getFromTypeEnvironment(node.procedureName);
        if(found == null)
        {
            errors.add(new Error("Procedure " + node.procedureName + " not declared in scope"));
            return "error";
        }

        if(node.arguments.size() != found.types.size())
        {
            errors.add(new Error("Mismatch between number of formal and actual parameters of procedure " + node.procedureName));
            return "error";
        }

        for(int i = 0; i < node.arguments.size(); i++)
        {
            ProcArgumentNode procArgumentNode = node.arguments.get(i);
            if(procArgumentNode.exprNode != null)
            {
                String typeAccept = (String) procArgumentNode.exprNode.accept(this);
                boolean isCompatibleType = found.types.get(i).equals(typeAccept) || (found.types.get(i).equals("real") && typeAccept.equals("integer"));
                if(!isCompatibleType)
                {
                    errors.add((new Error((i+1) + "° parameter should be " + found.types.get(i) + " but is " + typeAccept + " in procedure call " + node.procedureName)));
                    type = "error";
                }
            }
            else if(procArgumentNode.variableReferenced != null)
            {
                IdNode idNode = new IdNode(procArgumentNode.variableReferenced);
                idNode.accept(this);
                Record foundVar = top.getFromTypeEnvironment(procArgumentNode.variableReferenced);
                if(foundVar != null)
                {
                    String typeVar = "out " + foundVar.type;
                    if(!(typeVar.equals(found.types.get(i))))
                    {
                        errors.add(new Error((i+1) + "° parameter should be " + found.types.get(i) + " but is " + typeVar + " in procedure call " +  node.procedureName));
                        type = "error";
                    }
                }
            }
        }


        return type;
    }

    @Override
    public Object visit(ReadStatementNode node) {
        String type = "notype";
        for(ExprNode exprNode : node.exprs)
            if(exprNode.isDollar)
                if(!(exprNode.operator.equals("id")))
                {
                    errors.add(new Error("Read parameters must be identifiers"));
                    type = "error";
                }

        for(ExprNode exprNode : node.exprs)
        {
            String typeAccept = (String) exprNode.accept(this);
            if(typeAccept.equals("error"))
                type = "error";
        }

        return type;
    }

    @Override
    public Object visit(ReturnStatementNode node) {
        String type = "notype";

        return type;
    }

    @Override
    public Object visit(WhileStatementNode node) {
        String type = "notype";
        String typeCondition = (String) node.condition.accept(this);
        if(typeCondition == null || !(typeCondition.equals("boolean")))
        {
            errors.add(new Error("while condition " + node.condition + " must be a boolean condition"));
            type = "error";
        }
        type = (String) node.body.accept(this); //enter scope previously created

        top = top.exitScope();

        return type;
    }

    @Override
    public Object visit(ForStatementNode node) {
        String type = "notype";

        type = (String) node.init.accept(this);

        String typeConditionInit = (String) node.init.expressions.get(0).accept(this);
        String typeConditionToInt = (String) node.toInt.accept(this);
        String typeConditionStep = (String) node.step.accept(this);
        if(typeConditionInit == null || !(typeConditionInit.equals("integer")))
        {
            errors.add(new Error("For variable " + node.variableName + " must be initialized to an integer expression"));
            type = "error";
        }

        if(typeConditionToInt == null || !(typeConditionToInt.equals("integer")))
        {
            errors.add(new Error("For \"to\" must be an integer expression"));
            type = "error";
        }

        if(typeConditionStep == null || !(typeConditionStep.equals("integer")))
        {
            errors.add(new Error("For \"step\" must be an integer expression"));
            type = "error";
        }

        if(type.equals("error"))
            node.body.accept(this);
        else
            type = (String) node.body.accept(this);

        top = top.exitScope();

        return type;
    }

    @Override
    public Object visit(WriteStatementNode node) {
        String type = "notype";
        for(ExprNode exprNode : node.expressions)
        {
            String typeAccept = (String) exprNode.accept(this);
            if(!exprNode.isDollar && (!(typeAccept.equals("string")) || exprNode.operator.equals("id")))
            {
                errors.add(new Error("Expressions (also ids) are used only in $"));
                type = "error";
            }
            if(typeAccept.equals("error"))
                type = "error";
        }

        return type;
    }

    @Override
    public Object visit(BodyNode node) {
        String type = "notype";
        top = node.environment; //enter scope previously created
        for(Node n : node.nodes)
        {
            String typeAccept = (String) n.accept(this);
            if(typeAccept.equals("error"))
                type = "error";
        }

        return type;
    }

    @Override
    public Object visit(ConstNode node) {
        String type = node.value.getClass().getSimpleName().toLowerCase();
        if(type.equals("float"))
            type = "real";
        if(type.equals("character"))
            type = "char";

        return type;
    }

    @Override
    public Object visit(ElifNode node) {
        String type = "notype";
        String typeCondition = (String) node.condition.accept(this);
        if(typeCondition == null || !(typeCondition.equals("boolean")))
        {
            errors.add(new Error("elif condition " + node.condition + " must be a boolean condition"));
            type = "error";
        }

        type = (String) node.body.accept(this);
        top = top.exitScope();

        return type;
    }

    @Override
    public Object visit(ExprNode node) {
        if(node.node1 instanceof ExprNode && node.node2 instanceof ExprNode)
        {
            String returnType;
            FunCallNode funCallNode;
            ExprNode exprNode1 = (ExprNode) node.node1;
            ExprNode exprNode2 = (ExprNode) node.node2;

            if(exprNode1.node1 instanceof FunCallNode)
            {
                funCallNode = (FunCallNode) exprNode1.node1;
                returnType = (String) funCallNode.accept(this);
                if(returnType.contains("|"))
                {
                    errors.add(new Error("Can't use function " + funCallNode.functionName + " with multiple return types in expressions"));
                    return "error";
                }
            }

            if(exprNode2.node1 instanceof FunCallNode)
            {
                funCallNode = (FunCallNode) exprNode2.node1;
                returnType = (String) funCallNode.accept(this);
                if(returnType.contains("|"))
                {
                    errors.add(new Error("Can't use function " + funCallNode.functionName + " with multiple return types in expressions"));
                    return "error";
                }
            }
        }

        Object node1Accept;
        Object node2Accept = null;
        node1Accept = node.node1.accept(this);
        if(node.node2 != null)
            node2Accept = node.node2.accept(this);

        if(node1Accept.equals("error"))
            return "error";

        if(node.node1 instanceof ConstNode)
        {
            node.nodeType = (String) node1Accept;
            return node.nodeType;
        }

        if(node.node1 instanceof IdNode)
        {
            node.nodeType = (String) node1Accept;
            return node.nodeType;
        }

        if(node.node1 instanceof FunCallNode)
        {
            /*
            FunCallNode funCall = (FunCallNode) node.node1;
            String returnType = (String) node1Accept;
            String[] typesArray = returnType.split("\\|");
            ArrayList<String> returnTypes = new ArrayList<>(Arrays.asList(typesArray));
            if(returnTypes.size() > 1)
            {
                errors.add(new Error("Can't use function " + funCall.functionName + " with multiple return types in expressions"));
                return "error";
            }*/
            node.nodeType = (String) node1Accept;
            return node.nodeType;
        }

        //parenthesis
        if(node.operator.equals("pare"))
        {
            node.nodeType = (String) node1Accept;
            return node.nodeType;
        }

        //unary operators
        if(node.operator.equals("uminus"))
        {
            String returnTable = opTypeTable.searchOp(node.operator, (String) node1Accept, "");
            if(returnTable.equals("error"))
                errors.add(new Error("unary minus can't be used with " + node1Accept));

            node.nodeType = returnTable;
            return node.nodeType;
        }

        if(node.operator.equals("not"))
        {
            String returnTable = opTypeTable.searchOp(node.operator, (String) node1Accept, "");
            if(returnTable.equals("error"))
                errors.add(new Error("not operator can't be used with " + node1Accept));

            node.nodeType = returnTable;
            return node.nodeType;
        }



        node.nodeType = opTypeTable.searchOp(node.operator, (String) node1Accept, (String) node2Accept);
        return node.nodeType;
    }

    @Override
    public Object visit(FunCallNode node) {
        boolean foundError = false;
        Record found = top.getFromTypeEnvironment(node.functionName);
        if(found == null)
        {
            errors.add(new Error("Function " + node.functionName + " not declared in scope"));
            return "error";
        }

        if(node.arguments.size() != found.types.size())
        {
            errors.add(new Error("Mismatch between number of formal and actual parameters of function " + node.functionName));
            return "error";
        }

        for(int i = 0; i < node.arguments.size(); i++)
        {
            String typeAccept = (String) node.arguments.get(i).accept(this);
            boolean isCompatibleType = found.types.get(i).equals(typeAccept) || (found.types.get(i).equals("real") && typeAccept.equals("integer"));
            if(!isCompatibleType)
            {
                errors.add(new Error((i+1) + "° parameter should be " + found.types.get(i) + " but is " + typeAccept + " in function call " + node.functionName));
                foundError = true;
            }

        }

        if(foundError)
            return "error"; //handle multiple error in parameters types

        if(found.returnTypes.size() == 1) //Function call with 1 return value and can be used in expressions
            return found.returnTypes.get(0);

        return String.join("|", found.returnTypes); //Function call >1 values and so can be used only in assignments
    }

    @Override
    public Object visit(IdNode node) {
        Record found = top.getFromTypeEnvironment(node.name);
        if(found == null)
        {
            errors.add(new Error("Variable " + node.name + " not declared in scope"));
            return "error";
        }
        else if(!(found.kind.equals("variable")))
        {
            errors.add(new Error(found.name + " is not a variable, can't be used in expressions"));
            return "error";
        }

        return found.type;
    }

    @Override
    public Object visit(ProcArgumentNode node) {
        String type = "notype";

        return type;
    }


}

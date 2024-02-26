package visitor;

import nodes.*;
import nodes.statements.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XMLVisitor implements Visitor{

    private Document document;
    public XMLVisitor() throws ParserConfigurationException
    {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    public void saveDocument(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(System.getProperty("user.dir") + File.separator + "albero_sintattico.xml"));
        transformer.transform(domSource, streamResult);
    }

    @Override
    public Object visit(ProgramNode node) {
        Element programElement = document.createElement("Program");
        Element e;

        Element varDecls = document.createElement("VarDecls");
        for(VarDeclNode varDeclNode : node.varDeclarations)
        {
            e = (Element) varDeclNode.accept(this);
            varDecls.appendChild(e);
        }
        programElement.appendChild(varDecls);

        Element functions = document.createElement("Functions");
        for(FunctionNode functionNode : node.functions)
        {
            e = (Element) functionNode.accept(this);
            functions.appendChild(e);
        }
        programElement.appendChild(functions);

        Element procedures = document.createElement("Procedures");
        for(ProcedureNode procedureNode : node.procedures)
        {
            e = (Element) procedureNode.accept(this);
            procedures.appendChild(e);
        }
        programElement.appendChild(procedures);


        document.appendChild(programElement);
        return document;
    }

    @Override
    public Object visit(VarDeclNode node) {
        Element varDeclElement = document.createElement("VarDecl");
        Element e;

        for(VarSingleDeclNode varSingleDeclNode : node.declarations)
        {
            e = (Element) varSingleDeclNode.accept(this);
            varDeclElement.appendChild(e);
        }

        return varDeclElement;
    }

    @Override
    public Object visit(VarSingleDeclNode node) {
        Element varSingleDeclElement = document.createElement("VarSingleDecl");
        Element e;

        for(IdNode idNode : node.identifiers)
        {
            e = (Element) idNode.accept(this);
            varSingleDeclElement.appendChild(e);
        }

        if(node.type != null)
        {
            Text text = (Text) document.createTextNode(node.type);
            varSingleDeclElement.appendChild(text);
        }
        else
        {
            for(ConstNode constNode : node.initialValues)
            {
                e = (Element) constNode.accept(this);
                varSingleDeclElement.appendChild(e);
            }
        }

        return varSingleDeclElement;
    }

    @Override
    public Object visit(FunctionNode node) {
        Element functionElement = document.createElement("Function");
        Element e;

        Text text = (Text) document.createTextNode(node.name);
        functionElement.appendChild(text);

        for(IdNode idNode : node.parameters)
        {
            e = (Element) idNode.accept(this);
            functionElement.appendChild(e);
        }

        for(String returns : node.returnTypes)
        {
            text = (Text) document.createTextNode(returns);
            functionElement.appendChild(text);
        }

        e = (Element) node.body.accept(this);
        functionElement.appendChild(e);

        return functionElement;
    }

    @Override
    public Object visit(ProcedureNode node) {
        Element procedureElement = document.createElement("Procedure");
        Element e;

        Text text = (Text) document.createTextNode(node.name);
        procedureElement.appendChild(text);

        if(node.parameters != null)
        {
            for(IdNode idNode : node.parameters)
            {
                e = (Element) idNode.accept(this);
                procedureElement.appendChild(e);
            }
        }

        e = (Element) node.body.accept(this);
        procedureElement.appendChild(e);

        return procedureElement;
    }

    @Override
    public Object visit(AssignStatementNode node) {
        Element assignStatElement = document.createElement("Assign");
        Element e;

        for(IdNode idNode : node.ids)
        {
            e = (Element) idNode.accept(this);
            assignStatElement.appendChild(e);
        }

        for(ExprNode exprNode : node.expressions)
        {
            e = (Element) exprNode.accept(this);
            assignStatElement.appendChild(e);
        }

        return assignStatElement;
    }

    @Override
    public Object visit(IfStatementNode node) {
        Element ifStatElement = document.createElement("If");
        Element e;

        e = (Element) node.condition.accept(this);
        ifStatElement.appendChild(e);

        e = (Element) node.thenBody.accept(this);
        ifStatElement.appendChild(e);

        if(node.elifs != null)
        {
            for(ElifNode elifNode : node.elifs)
            {
                e = (Element) elifNode.accept(this);
                ifStatElement.appendChild(e);
            }
        }

        if(node.elseBody != null)
        {
            e = (Element) node.elseBody.accept(this);
            ifStatElement.appendChild(e);
        }

        return ifStatElement;
    }

    @Override
    public Object visit(ProcCallNode node) {
        Element procCallElement = document.createElement("ProcCall");
        Element e;

        Text text = (Text) document.createTextNode(node.procedureName);
        procCallElement.appendChild(text);

        for(ProcArgumentNode procArgumentNode : node.arguments)
        {
            e = (Element) procArgumentNode.accept(this);
            procCallElement.appendChild(e);
        }

        return procCallElement;
    }

    @Override
    public Object visit(ReadStatementNode node) {
        Element readStatElement = document.createElement("Read");
        Element e;

        for(ExprNode exprNode : node.exprs)
        {
            e = (Element) exprNode.accept(this);
            readStatElement.appendChild(e);
        }

        return readStatElement;
    }

    @Override
    public Object visit(ReturnStatementNode node) {
        Element returnStatElement = document.createElement("Return");
        Element e;

        for(ExprNode exprNode : node.returnExpressions)
        {
            e = (Element) exprNode.accept(this);
            returnStatElement.appendChild(e);
        }

        return returnStatElement;
    }

    @Override
    public Object visit(WhileStatementNode node) {
        Element whileStatElement = document.createElement("While");
        Element e;

        e = (Element) node.condition.accept(this);
        whileStatElement.appendChild(e);

        e = (Element) node.body.accept(this);
        whileStatElement.appendChild(e);

        return  whileStatElement;
    }

    @Override
    public Object visit(WriteStatementNode node) {
        Element writeStatElement = document.createElement("Write");
        Element e;

        for(ExprNode exprNode : node.expressions)
        {
            e = (Element) exprNode.accept(this);
            writeStatElement.appendChild(e);
        }

        Text text = (Text) document.createTextNode("new line: " + String.valueOf(node.newLine));
        writeStatElement.appendChild(text);

        return writeStatElement;
    }

    @Override
    public Object visit(BodyNode node) {
        Element bodyElement = document.createElement("Body");
        Element e;

        for(Node n : node.nodes)
        {
            e = (Element) n.accept(this);
            bodyElement.appendChild(e);
        }

        return bodyElement;
    }

    @Override
    public Object visit(ConstNode node) {
        Element constElement = document.createElement("Const");
        Element e;

        Text text = (Text) document.createTextNode(String.valueOf(node.value));
        constElement.appendChild(text);

        return constElement;
    }

    @Override
    public Object visit(ElifNode node) {
        Element elifStatElement = document.createElement("Elif");
        Element e;

        e = (Element) node.condition.accept(this);
        elifStatElement.appendChild(e);

        e = (Element) node.body.accept(this);
        elifStatElement.appendChild(e);

        return elifStatElement;
    }

    @Override
    public Object visit(ExprNode node) {
        Element exprElement = document.createElement("Expr");
        Element e;

        e = (Element) node.node1.accept(this);
        exprElement.appendChild(e);

        Text text = (Text) document.createTextNode(node.operator);
        exprElement.appendChild(text);

        if(node.node2 != null)
        {
            e = (Element) node.node2.accept(this);
            exprElement.appendChild(e);
        }

        return exprElement;
    }

    @Override
    public Object visit(FunCallNode node) {
        Element funcCallElement = document.createElement("FunCall");
        Element e;

        Text text = (Text) document.createTextNode(node.functionName);
        funcCallElement.appendChild(text);

        for(ExprNode exprNode : node.arguments)
        {
            e = (Element) exprNode.accept(this);
            funcCallElement.appendChild(e);
        }

        return funcCallElement;
    }

    @Override
    public Object visit(IdNode node) {
        Element idElement = document.createElement("ID");
        Element e;

        Text text = (Text) document.createTextNode(node.name + " ");
        idElement.appendChild(text);

        text = (Text) document.createTextNode(node.idType +  " ");
        idElement.appendChild(text);

        text = (Text) document.createTextNode("out: " + String.valueOf( node.isOut));
        idElement.appendChild(text);

        return idElement;
    }


    @Override
    public Object visit(ProcArgumentNode node) {
        Element procArgumentElement = document.createElement("ProcArgument");
        Element e;

        if(node.exprNode != null)
        {
            e = (Element) node.exprNode.accept(this);
            procArgumentElement.appendChild(e);
        }
        else if(node.variableReferenced != null)
        {
            Text text = (Text) document.createTextNode("ref: " + node.variableReferenced);
            procArgumentElement.appendChild(text);
        }

        return procArgumentElement;
    }
}

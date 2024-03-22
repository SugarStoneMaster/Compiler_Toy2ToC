package visitor;

import java.util.HashMap;
import java.util.Map;

public class OpTypeTable {
    private final Map<OpKey, String> opTypeMap;

    public OpTypeTable() {
        this.opTypeMap = new HashMap<>();

        opTypeMap.put(new OpKey("add", "string", "string"), "string");

        opTypeMap.put(new OpKey("add", "real", "string"), "string");
        opTypeMap.put(new OpKey("add", "string", "real"), "string");
        opTypeMap.put(new OpKey("add", "integer", "string"), "string");
        opTypeMap.put(new OpKey("add", "string", "integer"), "string");
        opTypeMap.put(new OpKey("add", "boolean", "string"), "string");
        opTypeMap.put(new OpKey("add", "string", "boolean"), "string");



        opTypeMap.put(new OpKey("add", "integer", "integer"), "integer");
        opTypeMap.put(new OpKey("sub", "integer", "integer"), "integer");
        opTypeMap.put(new OpKey("times", "integer", "integer"), "integer");
        opTypeMap.put(new OpKey("div", "integer", "integer"), "integer");

        opTypeMap.put(new OpKey("add", "real", "integer"), "real");
        opTypeMap.put(new OpKey("sub", "real", "integer"), "real");
        opTypeMap.put(new OpKey("times", "real", "integer"), "real");
        opTypeMap.put(new OpKey("div", "real", "integer"), "real");

        opTypeMap.put(new OpKey("add", "integer", "real"), "real");
        opTypeMap.put(new OpKey("sub", "integer", "real"), "real");
        opTypeMap.put(new OpKey("times", "integer", "real"), "real");
        opTypeMap.put(new OpKey("div", "integer", "real"), "real");

        opTypeMap.put(new OpKey("add", "real", "real"), "real");
        opTypeMap.put(new OpKey("sub", "real", "real"), "real");
        opTypeMap.put(new OpKey("times", "real", "real"), "real");
        opTypeMap.put(new OpKey("div", "real", "real"), "real");


        opTypeMap.put(new OpKey("and", "boolean", "boolean"), "boolean");
        opTypeMap.put(new OpKey("or", "boolean", "boolean"), "boolean");
        opTypeMap.put(new OpKey("eq", "boolean", "boolean"), "boolean");
        opTypeMap.put(new OpKey("ne", "boolean", "boolean"), "boolean");


        opTypeMap.put(new OpKey("eq", "string", "string"), "boolean");
        opTypeMap.put(new OpKey("ne", "string", "string"), "boolean");

        opTypeMap.put(new OpKey("eq", "integer", "integer"), "boolean");
        opTypeMap.put(new OpKey("ne", "integer", "integer"), "boolean");
        opTypeMap.put(new OpKey("lt", "integer", "integer"), "boolean");
        opTypeMap.put(new OpKey("le", "integer", "integer"), "boolean");
        opTypeMap.put(new OpKey("gt", "integer", "integer"), "boolean");
        opTypeMap.put(new OpKey("ge", "integer", "integer"), "boolean");

        opTypeMap.put(new OpKey("eq", "char", "char"), "boolean");
        opTypeMap.put(new OpKey("ne", "char", "char"), "boolean");
        opTypeMap.put(new OpKey("lt", "char", "char"), "boolean");
        opTypeMap.put(new OpKey("le", "char", "char"), "boolean");
        opTypeMap.put(new OpKey("gt", "char", "char"), "boolean");
        opTypeMap.put(new OpKey("ge", "char", "char"), "boolean");

        opTypeMap.put(new OpKey("eq", "real", "integer"), "boolean");
        opTypeMap.put(new OpKey("ne", "real", "integer"), "boolean");
        opTypeMap.put(new OpKey("lt", "real", "integer"), "boolean");
        opTypeMap.put(new OpKey("le", "real", "integer"), "boolean");
        opTypeMap.put(new OpKey("gt", "real", "integer"), "boolean");
        opTypeMap.put(new OpKey("ge", "real", "integer"), "boolean");

        opTypeMap.put(new OpKey("eq", "integer", "real"), "boolean");
        opTypeMap.put(new OpKey("ne", "integer", "real"), "boolean");
        opTypeMap.put(new OpKey("lt", "integer", "real"), "boolean");
        opTypeMap.put(new OpKey("le", "integer", "real"), "boolean");
        opTypeMap.put(new OpKey("gt", "integer", "real"), "boolean");
        opTypeMap.put(new OpKey("ge", "integer", "real"), "boolean");

        opTypeMap.put(new OpKey("eq", "real", "real"), "boolean");
        opTypeMap.put(new OpKey("ne", "real", "real"), "boolean");
        opTypeMap.put(new OpKey("lt", "real", "real"), "boolean");
        opTypeMap.put(new OpKey("le", "real", "real"), "boolean");
        opTypeMap.put(new OpKey("gt", "real", "real"), "boolean");
        opTypeMap.put(new OpKey("ge", "real", "real"), "boolean");


        opTypeMap.put(new OpKey("uminus", "integer", ""), "integer");
        opTypeMap.put(new OpKey("uminus", "real", ""), "real");

        opTypeMap.put(new OpKey("not", "boolean", ""), "boolean");
    }

    public String searchOp(String operation, String firstOperand, String secondOperand) {
        String typeFound = opTypeMap.get(new OpKey(operation, firstOperand, secondOperand));
        if(typeFound != null)
            return typeFound;
        else
            return "error";
    }
}


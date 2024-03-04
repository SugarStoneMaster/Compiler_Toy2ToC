package visitor;

import nodes.Node;

import java.util.*;

public class Environment {
    public Table<String, Record> table;

    public Environment prev;

    public Environment(Environment prev) {
        table = new Table<>();
        this.prev = prev;
    }

    public void addId(String name, String kind, String type, boolean isOut, boolean isFuncParam)
    {
        Record entry = getFromThisTable(name);
        if(entry == null)
            table.put(name, new Record(name, kind, type, isOut, isFuncParam));
        else if (entry != null && entry.kind.equals("variable"))
            throw new Error("Variable " + name + " already declared");
    }

    public void addId(String name, String kind, ArrayList<String> types)
    {
        Record entry = getFromThisTable(name);
        if(entry == null)
            table.put(name, new Record(name, kind, types));
        else if (entry != null && entry.kind.equals("procedure"))
            throw new Error("Procedure " + name + " already declared");
    }

    public void addId(String name, String kind, ArrayList<String> types, ArrayList<String> returnTypes)
    {
        Record entry = getFromThisTable(name);
        if(entry == null)
            table.put(name, new Record(name, kind, types, returnTypes));
        else if (entry != null && entry.kind.equals("function"))
            throw new Error("Function " + name + " already declared");
    }


    public Record getFromTypeEnvironment(String name)
    {
        for( Environment e = this; e != null; e = e.prev ) {
            Record found = e.table.get(name);
            if( found != null ) return found;
        }

        return null;
    }

    public Record getFromThisTable(String name)
    {
        return table.get(name);
    }

    public Environment createAndEnterScope() {
        return new Environment(this);
    }

    public Environment exitScope()
    {
        return this.prev;
    }

    @Override
    public String toString() {
        StringBuffer string = new StringBuffer();
        int num = 1;
        for( Environment e = this; e != null; e = e.prev ) {
            string.append("<<" + e.table.name + ">>" + "\n");
            Set<String> keys = e.table.keySet();
            for(String key : keys)
            {
                Record entry = e.table.get(key);
                string.append(entry.toString(entry.kind) + "\n");
            }

            string.append("\n");
        }

        return string.toString();
    }
}

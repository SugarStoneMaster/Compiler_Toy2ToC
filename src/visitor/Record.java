package visitor;

import java.util.ArrayList;

public class Record {
    public String name;

    public String kind;

    public String type; //only for variables

    public ArrayList<String> types; //only for functions and procedures

    public ArrayList<String> returnTypes; //only for functions

    public boolean isOut; //variable that is referenced in a procedure as a parameter


    //for variables
    public Record(String name, String kind, String type, boolean isOut) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.isOut = isOut;
    }

    //for procedures
    public Record(String name, String kind, ArrayList<String> types) {
        this.name = name;
        this.kind = kind;
        this.types = types;
    }

    //for functions
    public Record(String name, String kind, ArrayList<String> types, ArrayList<String> returnTypes) {
        this.name = name;
        this.kind = kind;
        this.types = types;
        this.returnTypes = returnTypes;
    }

    public String toString(String kind) {
        if(kind.equals("variable"))
            return "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", type='" + type + '\'' +
                    ", isOut=" + isOut +
                    '}';
        else if(kind.equals("procedure"))
            return "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", types=" + types +
                    '}';
        else if(kind.equals("function"))
            return "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", types=" + types +
                    ", returnTypes=" + returnTypes +
                    '}';

        return null;
    }

}

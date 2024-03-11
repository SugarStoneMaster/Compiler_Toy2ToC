package visitor;

import java.util.ArrayList;

public class Record {
    public String name;

    public String kind;

    public String type; //only for variables

    public ArrayList<String> types = new ArrayList<>(); //only for functions and procedures

    public ArrayList<String> returnTypes = new ArrayList<>(); //only for functions

    public boolean isOut; //variable that is referenced in a procedure as a parameter

    public boolean isFuncParam;


    //for variables
    public Record(String name, String kind, String type, boolean isOut, boolean isFuncParam) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.isOut = isOut;
        this.isFuncParam = isFuncParam;
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
        return switch (kind) {
            case "variable" -> "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", type='" + type + '\'' +
                    ", isOut=" + isOut +
                    ", isFuncParam=" + isFuncParam +
                    '}';
            case "procedure" -> "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", types=" + types +
                    '}';
            case "function" -> "Record{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", types=" + types +
                    ", returnTypes=" + returnTypes +
                    '}';
            default -> null;
        };

    }

}

package visitor;

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

    public HashSet<String> getAllSymbolsFromTypeEnvironment()
    {
        HashSet<String> symbols = new HashSet<>();
        for(Environment e = this; e != null; e = e.prev)
        {
            Collection<String> records = e.table.keySet();
            symbols.addAll(records);
        }

        return symbols;
    }

    public void removeLangKeywords(HashSet<String> symbols, ArrayList<String> langKeywords) //in this case, because the translation is from Toy2 to C, "Lang" is C
    {
        for(Environment e = this; e != null; e = e.prev)
        {
            Table<String, Record> currentTable = e.table;
            for(String langKeyword : langKeywords)
            {
                if(symbols.contains(langKeyword))
                {
                    Record found = (Record) currentTable.get(langKeyword);
                    if(found != null)
                    {
                        String newName;
                        int i = 1;
                        while(symbols.contains(newName = generateNewIdentifierForKeyword(langKeyword, i))) i++;
                        found.name = newName;
                    }

                }
            }
        }
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




    private static String generateRandomIdentifier() {
        final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final int MAX_LENGTH = 10;
        Random random = new Random();
        StringBuilder identifier = new StringBuilder();

        // Add a random letter as the first character
        identifier.append(LETTERS.charAt(random.nextInt(LETTERS.length())));

        // Generate a random string of length 0 to MAX_LENGTH - 1
        int randomLength = random.nextInt(MAX_LENGTH - 1) + 1;
        for (int i = 0; i < randomLength; i++) {
            // Append a random letter or digit
            identifier.append(random.nextInt(2) == 0 ? LETTERS.charAt(random.nextInt(LETTERS.length())) : DIGITS.charAt(random.nextInt(DIGITS.length())));
        }

        return identifier.toString();
    }

    private String generateNewIdentifierForKeyword(String keyword, int i)
    {
        char lastChar = keyword.charAt(keyword.length() - i);
        keyword += lastChar;
        return keyword;
    }

}

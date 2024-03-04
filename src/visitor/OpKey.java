package visitor;

import java.util.Objects;

public class OpKey {
    private final String operation;
    private final String firstOperand;
    private final String secondOperand;

    public OpKey(String operation, String firstOperand, String secondOperand) {
        this.operation = operation;
        this.firstOperand = firstOperand;
        this.secondOperand = secondOperand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpKey opKey = (OpKey) o;
        return operation.equals(opKey.operation) &&
                firstOperand.equals(opKey.firstOperand) &&
                secondOperand.equals(opKey.secondOperand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, firstOperand, secondOperand);
    }
}


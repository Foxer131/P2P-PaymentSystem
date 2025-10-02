package com.p2ppayment.filegenerators;

public abstract class cnabBlank {
    protected String padRight(String input, int length) {
        if (input == null) input = "";
        if (input.length() > length) return input.substring(0, length);
        return String.format("%-" + length + "s", input);
    }

    protected String padLeft(String input, int length) {
        if (input == null) input = "";
        if (input.length() > length) return input.substring(0, length);
        return String.valueOf('0').repeat(length - input.length()) + input;
    }
}

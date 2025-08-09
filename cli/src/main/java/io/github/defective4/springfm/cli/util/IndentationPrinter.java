package io.github.defective4.springfm.cli.util;

import java.util.Objects;

public class IndentationPrinter {
    private int indentation = 0;
    private String indentationString = "  ";
    private String listString = "-";

    public IndentationPrinter() {
    }

    public IndentationPrinter(int indentation, String indentationString, String listString) {
        this.indentation = indentation;
        this.indentationString = indentationString;
        this.listString = listString;
    }

    public int getIndentation() {
        return indentation;
    }

    public String getIndentationString() {
        return indentationString;
    }

    public String getListString() {
        return listString;
    }

    public void indentationDown() {
        if (indentation > 0) indentation--;
    }

    public void indentationUp() {
        indentation++;
    }

    public void println(String line) {
        println(line, false);
    }

    public void printls(String line) {
        println(line, true);
    }

    public void setIndentation(int indentation) {
        if (indentation < 0) throw new IllegalArgumentException("indentation < 0");
        this.indentation = indentation;
    }

    public void setIndentationString(String indentationString) {
        this.indentationString = Objects.requireNonNull(indentationString);
    }

    public void setListString(String listString) {
        this.listString = Objects.requireNonNull(listString);
    }

    private void println(String line, boolean list) {
        if (indentation > 0) System.out.print(indentationString.repeat(indentation));
        if (list) System.out.print(listString + " ");
        System.out.println(line);
    }

}

package com.github.rvesse.airline.examples.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.rvesse.airline.Arguments;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.examples.ExampleRunnable;
import com.github.rvesse.airline.io.AnsiControlCodes;
import com.github.rvesse.airline.io.output.ColorizedOutputStream;

public abstract class ColorDemo<T> implements ExampleRunnable {

    @Arguments(description = "Provides the example text to use")
    private List<String> args = new ArrayList<String>();

    @Option(name = { "-b", "--background" }, description = "When set changes the background rather than the foreground colour")
    private boolean background = false;

    @Option(name = { "--reset" }, description = "When set rather than outputting colours only the relevant escape sequence for hard resetting your terminal is output.  This is useful if you've used one of the colour demos that uses features your terminal does not support and have got your terminal in a strange color state as a result")
    private boolean hardReset = false;

    public ColorDemo() {
        super();
    }

    protected abstract ColorizedOutputStream<T> openOutputStream();

    protected abstract T[] getColors();

    @Override
    public int run() {
        if (this.hardReset) {
            System.out.println(AnsiControlCodes.getGraphicsResetCode());
            System.out.println("Your terminal was reset");
            return 0;
        }

        ColorizedOutputStream<T> output = openOutputStream();
        try {
            System.out.println("If your terminal supports it the subsequent output will be colorized");

            String text = StringUtils.join(args, " ");
            if (StringUtils.isEmpty(text)) {
                text = "Sample text";
            }

            T[] colors = getColors();
            System.out.println("Demoing " + colors.length + " available colours");
            for (T color : colors) {
                // This text will appear in your terminal default colour
                System.out.format("Color %s:\n", color.toString());

                // Set the colour
                if (this.background) {
                    output.setBackgroundColor(color);
                } else {
                    output.setForegroundColor(color);
                }

                // Anything we write now will be appropriately colorized
                output.print(text);

                // Reset back to default color
                if (this.background) {
                    output.resetBackgroundColor();
                } else {
                    output.resetForegroundColor();
                }
                output.println();
            }

            return 0;
        } finally {
            // Just in case we hit an error remember to reset the color
            // appropriately
            if (this.background) {
                output.resetBackgroundColor();
            } else {
                output.resetForegroundColor();
            }
            System.out.println();
            output.close();
        }
    }

}
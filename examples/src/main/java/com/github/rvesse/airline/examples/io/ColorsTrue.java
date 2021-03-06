package com.github.rvesse.airline.examples.io;

import java.util.ArrayList;
import java.util.List;

import com.github.rvesse.airline.Command;
import com.github.rvesse.airline.Option;
import com.github.rvesse.airline.examples.ExampleExecutor;
import com.github.rvesse.airline.io.colors.TrueColor;
import com.github.rvesse.airline.io.output.AnsiTrueColorizedOutputStream;
import com.github.rvesse.airline.io.output.ColorizedOutputStream;

@Command(name = "colors-true", description = "Displays some text in true (24 bit) color (if your terminal supports this)")
public class ColorsTrue extends ColorDemo<TrueColor> {
    
    @Option(name = "--red", title = "Red", description = "Sets the desired red component")
    private int red = -1;
    
    @Option(name = "--green", title = "Green", description = "Sets the desired green component")
    private int green = -1;
    
    @Option(name = "--blue", title = "Blue", description = "Sets the desired blue component")
    private int blue = -1;

    //@formatter:off
    private int[] values = {
        0,
        32,
        64,
        96,
        128,
        160,
        192,
        224,
        255
    };
    //@formatter:on

    public static void main(String[] args) {
        ExampleExecutor.executeSingleCommand(ColorsTrue.class, args);
    }

    @Override
    protected ColorizedOutputStream<TrueColor> openOutputStream() {
        return new AnsiTrueColorizedOutputStream(System.out);
    }
    
    private int getColor(int c) {
        if (c == -1) return 0;
        if (c > 255) return 255;
        return c;
    }

    @Override
    protected TrueColor[] getColors() {
        if (red != -1 || green != -1 || blue != -1) {
            return new TrueColor[] { new TrueColor(getColor(red), getColor(green), getColor(blue)) };
        }
        
        List<TrueColor> colors = new ArrayList<TrueColor>();
        for (int r = 0; r < values.length; r++) {
            for (int g = 0; g < values.length; g++) {
                for (int b = 0; b < values.length; b++) {
                    colors.add(new TrueColor(values[r], values[g], values[b]));
                }
            }
        }
        return colors.toArray(new TrueColor[colors.size()]);
    }
}

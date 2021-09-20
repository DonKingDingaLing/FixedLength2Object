package de.donkingdingaling.tools.fixedlength2object.processors.testclasses;

import de.donkingdingaling.tools.fixedlength2object.annotations.*;
import de.donkingdingaling.tools.fixedlength2object.converter.DoubleConverter;
import de.donkingdingaling.tools.fixedlength2object.converter.IntegerConverter;

@LengthEntity
public class Customer {
    @Padding(alignment = Alignment.LEFT)
    @FixedLengthField(order = 1, length = 40)
    private String name;
    @TypeInformation(type = Integer.class, converter = IntegerConverter.class)
    @FixedLengthField(order = 2, length = 10)
    private int amountOfOrders;
    @TypeInformation(type = Double.class, converter = DoubleConverter.class)
    @FixedLengthField(order = 3, length = 10)
    private double ratio;

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmountOfOrders() {
        return amountOfOrders;
    }

    public void setAmountOfOrders(int amountOfOrders) {
        this.amountOfOrders = amountOfOrders;
    }
}

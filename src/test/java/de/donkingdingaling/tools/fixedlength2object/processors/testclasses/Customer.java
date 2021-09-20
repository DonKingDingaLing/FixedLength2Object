package de.donkingdingaling.tools.fixedlength2object.processors.testclasses;

import de.donkingdingaling.tools.fixedlength2object.annotations.*;

@LengthEntity
public class Customer {
    @Padding(alignment = Alignment.RIGHT)
    @FixedLengthField(order = 1, length = 40)
    private String name;
    @FixedLengthField(order = 2, length = 10)
    private int amountOfOrders;
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
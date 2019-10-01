package io.github.cshadd.location;

public class HistoricalElement {
    public float light;
    public String location;

    public HistoricalElement() {
        this(0, "NaN");
        return;
    }

    public HistoricalElement(float light, String location) {
        super();
        this.light = light;
        this.location = location;
        return;
    }
}

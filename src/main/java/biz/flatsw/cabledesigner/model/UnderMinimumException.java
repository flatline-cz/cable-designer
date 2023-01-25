package biz.flatsw.cabledesigner.model;

public class UnderMinimumException extends RuntimeException {
    private final Pin pin;
    private final float crossSection;
    private final float insulationDiameter;

    public UnderMinimumException(Pin pin, float crossSection, float insulationDiameter) {
        this.pin = pin;
        this.crossSection = crossSection;
        this.insulationDiameter = insulationDiameter;
    }

    public Pin getPin() {
        return pin;
    }

    public float getCrossSection() {
        return crossSection;
    }

    public float getInsulationDiameter() {
        return insulationDiameter;
    }

    public String toString() {
        return "Under minimum: "+pin.getConnector().getName()+"/"+pin.getName()+", cross="+crossSection+", diameter="+insulationDiameter;
    }
}

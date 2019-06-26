package commons;

import io.yooksi.commons.define.PositiveRange;

@SuppressWarnings("unused")
class TestClass {

    interface accessibleFieldChecks {}

    @PositiveRange(max=1, groups= TestClass.accessibleFieldChecks.class)
    public final int publicField = 2;
    @PositiveRange(max=1, groups= TestClass.accessibleFieldChecks.class)
    private final int privateField = 2;

    @PositiveRange(max=100)
    public int positiveValue = 110;
    @PositiveRange(max=10)
    public final int rangeValue = 25;
    @PositiveRange(max=1)
    public final int negativeValue = -25;
}

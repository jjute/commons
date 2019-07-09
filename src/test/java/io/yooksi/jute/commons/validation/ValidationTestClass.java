package io.yooksi.jute.commons.validation;

import io.yooksi.jute.commons.define.PositiveRange;

@SuppressWarnings("unused")
class ValidationTestClass {

    interface accessibleFieldChecks {}

    @PositiveRange(max = 1, groups = accessibleFieldChecks.class)
    public final int publicField = 2;
    @PositiveRange(max = 1, groups = accessibleFieldChecks.class)
    private final int privateField = 2;

    @PositiveRange(max=100)
    public int positiveValue = 110;
    @PositiveRange(max=10)
    public final int rangeValue = 25;
    @PositiveRange(max=1)
    public final int negativeValue = -25;
}

package io.yooksi.commons.git;

import javax.validation.constraints.NotEmpty;

public abstract class SimpleCLOption implements GitCLOption {

    private final String option;

    public SimpleCLOption(String value) {
        this.option = value;
    }

    @Override
    public @NotEmpty String getDesignation() {
        return toString();
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String toString() {
        return option;
    }
}

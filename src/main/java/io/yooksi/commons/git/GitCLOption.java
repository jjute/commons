package io.yooksi.commons.git;

import org.jetbrains.annotations.Contract;
import javax.validation.constraints.NotEmpty;

abstract class GitCLOption {

    @Contract(pure = true)
    public abstract @NotEmpty String getDesignation();

    @Contract(pure = true)
    public abstract String getValue();

    @Contract(pure = true)
    public abstract String toString();
}

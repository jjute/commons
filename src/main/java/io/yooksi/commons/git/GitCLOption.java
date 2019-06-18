package io.yooksi.commons.git;

import org.jetbrains.annotations.Contract;
import javax.validation.constraints.NotEmpty;

public interface GitCLOption {

    @Contract(pure = true)
    @NotEmpty String getDesignation();

    @Contract(pure = true)
    String getValue();

    @Contract(pure = true)
    String toString();
}

package io.yooksi.commons.git;

public class BasicCLOption extends SimpleCLOption {

    public static BasicCLOption VERSION = new BasicCLOption("--version");
    public static BasicCLOption HELP = new BasicCLOption("--help");

    public BasicCLOption(String value) {
        super(value);
    }
}

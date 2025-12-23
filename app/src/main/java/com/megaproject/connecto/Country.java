package com.megaproject.connecto;

public class  Country {
    private String code;
    private String name;
    private String dialCode;
    private String flag;

    public Country(String code, String name, String dialCode, String flag) {
        this.code = code;
        this.name = name;
        this.dialCode = dialCode;
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDialCode() {
        return dialCode;
    }

    public String getFlag() {
        return flag;
    }
}

package com.theladders.avital.cc;

import java.util.Arrays;

/**
 * @author huisheng.jin
 * @date 2020/2/27.
 */
public enum JobType {
    JREQ("JReq"),
    ATS("ATS");

    private String name;

    JobType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static JobType fromName(String name) {
        return Arrays.stream(values())
                .filter(jobType -> jobType.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

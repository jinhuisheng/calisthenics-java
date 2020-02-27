package com.theladders.avital.cc;

/**
 * @author huisheng.jin
 * @date 2020/2/27.
 */
public enum JobType {
    JREQ("JReq"),
    ATS ("ATS");

    private String name;

    JobType(String name) {
        this.name = name;
    }
}

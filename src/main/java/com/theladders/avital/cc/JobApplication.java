package com.theladders.avital.cc;

import java.time.LocalDate;

/**
 * @author huisheng.jin
 * @date 2020/2/27.
 */
public class JobApplication {
    private final String jobName;
    private final String jobType;
    private final LocalDate applicationTime;
    private final String employerName;

    public JobApplication(String jobName, String jobType, LocalDate applicationTime, String employerName) {
        this.jobName = jobName;
        this.jobType = jobType;
        this.applicationTime = applicationTime;
        this.employerName = employerName;
    }
}

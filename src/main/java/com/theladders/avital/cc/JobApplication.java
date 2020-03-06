package com.theladders.avital.cc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author huisheng.jin
 * @date 2020/2/27.
 */
public class JobApplication {
    private final String jobName;
    private final String jobType;
    private final String applicationTime;
    private final String employerName;

    public JobApplication(String jobName, String jobType, LocalDate applicationTime, String employerName) {
        this.jobName = jobName;
        this.jobType = jobType;
        this.applicationTime = applicationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.employerName = employerName;
    }

    public String getJobName() {
        return jobName;
    }

    public String getApplicationTime() {
        return applicationTime;
    }

    public String getEmployerName() {
        return employerName;
    }
}

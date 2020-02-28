package com.theladders.avital.cc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huisheng.jin
 * @date 2020/2/28.
 */
public class FailedApplications {
    private List<JobApplication> failedApplications = new ArrayList<>();

    public void addFailedApplications(String employerName, String jobName, String jobType, LocalDate applicationTime) {
        failedApplications.add(new JobApplication(jobName, jobType, applicationTime, employerName));
    }

    public int getUnsuccessfulApplications(String employerName, String jobName) {
        return (int) failedApplications.stream()
                .filter(job -> job.getJobName().equals(jobName) && job.getEmployerName().equals(employerName))
                .count();
    }

}

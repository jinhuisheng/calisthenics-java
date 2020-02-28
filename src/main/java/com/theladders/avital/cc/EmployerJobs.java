package com.theladders.avital.cc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author huisheng.jin
 * @date 2020/2/28.
 */
public class EmployerJobs {
    private HashMap<String, List<Job>> employerJobs = new HashMap<>();

    private void addJob(String employerName, String jobName, String jobType) {
        List<Job> savedJobs = employerJobs.getOrDefault(employerName, new ArrayList<>());
        Job job = new Job(jobName, jobType);
        savedJobs.add(job);
        employerJobs.put(employerName, savedJobs);
    }

    List<Job> getEmployerJobs(String employerName) {
        return employerJobs.get(employerName);
    }
}

package com.theladders.avital.cc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author huisheng.jin
 * @date 2020/2/28.
 */
public class SeekerConcernJobs {
    private HashMap<String, List<Job>> seekerConcernJobs = new HashMap<>();

    public void saveSeekerConcernJob(String jobSeekerName, String jobName, String jobType) {
        List<Job> savedJobs = seekerConcernJobs.getOrDefault(jobSeekerName, new ArrayList<>());
        savedJobs.add(new Job(jobName, jobType));
        seekerConcernJobs.put(jobSeekerName, savedJobs);
    }

    public List<Job> getSeekerConcernJobs(String jobSeekerName) {
        return seekerConcernJobs.get(jobSeekerName);
    }
}

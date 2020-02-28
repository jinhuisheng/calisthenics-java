package com.theladders.avital.cc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author huisheng.jin
 * @date 2020/2/28.
 */
public class JobSeekerApplications {
    private HashMap<String, List<JobApplication>> jobSeekerApplications = new HashMap<>();

    public void addApply(String employerName, String jobName, String jobType, String jobSeekerName, LocalDate applicationTime) {
        List<JobApplication> savedJobApplications = jobSeekerApplications.getOrDefault(jobSeekerName, new ArrayList<>());
        JobApplication jobApplication = new JobApplication(jobName, jobType, applicationTime, employerName);
        savedJobApplications.add(jobApplication);
        jobSeekerApplications.put(jobSeekerName, savedJobApplications);
    }

    List<JobApplication> getJobSeekerApplications(String employerName) {
        return jobSeekerApplications.get(employerName);
    }

    public List<String> findApplicants(String jobName, LocalDate from, LocalDate to) {
        Predicate<JobApplication> predicate = queryCondition(jobName, from, to);
        return getApplicants(predicate);
    }

    private Predicate<JobApplication> queryCondition(String jobName, LocalDate from, LocalDate to) {
        List<Predicate<JobApplication>> allPredicates = new ArrayList<>();
        if (jobName != null) {
            allPredicates.add(job -> job.getJobName().equals(jobName));
        }
        if (from != null) {
            allPredicates.add(job -> !from.isAfter(convertToDate(job.getApplicationTime())));
        }
        if (to != null) {
            allPredicates.add(job -> !to.isBefore(convertToDate(job.getApplicationTime())));
        }
        return allPredicates.stream().reduce(x -> true, Predicate::and);
    }

    private LocalDate convertToDate(String applicationTime) {
        return LocalDate.parse(applicationTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    private List<String> getApplicants(Predicate<JobApplication> predicate) {
        return this.jobSeekerApplications.entrySet().stream()
                .filter(set -> set.getValue().stream().anyMatch(predicate))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public int getSuccessfulApplications(String employerName, String jobName) {
        int newResult = 0;
        for (Map.Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
            List<JobApplication> jobs = set.getValue();
            newResult += jobs.stream()
                    .anyMatch(job -> job.getEmployerName().equals(employerName) && job.getJobName().equals(jobName)) ? 1 : 0;
        }

        return newResult;
    }

    /**
     * 导出已申请的数据
     *
     * @param type
     * @param date
     * @return
     */
    public String export(String type, LocalDate date) {
        Map<String, List<JobApplication>> exportData = getExportData(date);
        return Exporter.export(type, exportData);
    }

    private Map<String, List<JobApplication>> getExportData(LocalDate date) {
        return this.jobSeekerApplications.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> filter(date, e.getValue())));
    }

    private List<JobApplication> filter(LocalDate date, List<JobApplication> jobApplicationList) {
        return jobApplicationList.stream()
                .filter(job -> job.getApplicationTime().equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .collect(Collectors.toList());
    }

}

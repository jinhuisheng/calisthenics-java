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
        if (from == null && to == null) {
            return job -> job.getJobName().equals(jobName);
        }
        if (jobName == null && to == null) {
            return job ->
                    !from.isAfter(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (jobName == null && from == null) {
            return job ->
                    !to.isBefore(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (jobName == null) {
            return job -> !from.isAfter(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))) && !to.isBefore(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (to != null) {
            return job -> job.getJobName().equals(jobName) && !to.isBefore(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return job -> job.getJobName().equals(jobName) && !from.isAfter(LocalDate.parse(job.getApplicationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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
        if ("csv".equals(type)) {
            return exportCsv(date);
        } else {
            return exportHtml(date);
        }
    }

    private String exportHtml(LocalDate date) {
        StringBuilder newContent = new StringBuilder();
        for (Map.Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
            String applicant = set.getKey();
            newContent.append(set.getValue().stream()
                    .filter(job -> job.getApplicationTime().equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .map(job -> "<tr>" + "<td>" + job.getEmployerName() + "</td>" + "<td>" + job.getJobName() + "</td>" + "<td>" + job.getJobType() + "</td>" + "<td>" + applicant + "</td>" + "<td>" + job.getApplicationTime() + "</td>" + "</tr>")
                    .collect(Collectors.joining()));
        }

        return "<!DOCTYPE html>"
                + "<body>"
                + "<table>"
                + "<thead>"
                + "<tr>"
                + "<th>Employer</th>"
                + "<th>Job</th>"
                + "<th>Job Type</th>"
                + "<th>Applicants</th>"
                + "<th>Date</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + newContent
                + "</tbody>"
                + "</table>"
                + "</body>"
                + "</html>";
    }

    private String exportCsv(LocalDate date) {
        StringBuilder newResult = new StringBuilder("Employer,Job,Job Type,Applicants,Date" + "\n");
        for (Map.Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
            String applicant = set.getKey();
            newResult.append(set.getValue().stream()
                    .filter(job -> job.getApplicationTime().equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .map(job -> job.getEmployerName() + "," + job.getJobName() + "," + job.getJobType() + "," + applicant + "," + job.getApplicationTime() + "\n")
                    .collect(Collectors.joining()));
        }
        return newResult.toString();
    }

}

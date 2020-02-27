package com.theladders.avital.cc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Map.*;

public class Application {
    private final List<List<String>> failedApplications = new ArrayList<>();
    private HashMap<String, List<Job>> employerJobs = new HashMap<>();
    private HashMap<String, List<JobApplication>> jobSeekerApplications = new HashMap<>();


    public void execute(String command, String employerName, String jobName, String jobType, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime) throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        if (command == "publish") {
            checkJobTypeWhenPublish(jobType);
            addJob(employerName, jobName, jobType);
            return;
        }
        if (command == "save") {
            addJob(employerName, jobName, jobType);
        }
        if (command == "apply") {
            checkJobTypeWhenApplyCommand(employerName, jobName, jobType, jobSeekerName, resumeApplicantName, applicationTime);
            addApply(employerName, jobName, jobType, jobSeekerName, applicationTime);
        }
    }

    private void checkJobTypeWhenApplyCommand(String employerName, String jobName, String jobType, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime) throws RequiresResumeForJReqJobException, InvalidResumeException {
        if (jobType.equals("JReq") && resumeApplicantName == null) {
            addFailedApplications(employerName, jobName, jobType, applicationTime);
            throw new RequiresResumeForJReqJobException();
        }

        if (jobType.equals("JReq") && !resumeApplicantName.equals(jobSeekerName)) {
            throw new InvalidResumeException();
        }
    }

    private void checkJobTypeWhenPublish(String jobType) throws NotSupportedJobTypeException {
        if (!jobType.equals("JReq") && !jobType.equals("ATS")) {
            throw new NotSupportedJobTypeException();
        }
    }

    private void addFailedApplications(String employerName, String jobName, String jobType, LocalDate applicationTime) {
        List<String> failedApplication = new ArrayList<String>() {{
            add(jobName);
            add(jobType);
            add(applicationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            add(employerName);
        }};
        failedApplications.add(failedApplication);
    }

    private void addApply(String employerName, String jobName, String jobType, String jobSeekerName, LocalDate applicationTime) {
        List<JobApplication> savedJobApplications = jobSeekerApplications.getOrDefault(jobSeekerName, new ArrayList<>());
        JobApplication jobApplication = new JobApplication(jobName, jobType, applicationTime, employerName);
        savedJobApplications.add(jobApplication);
        jobSeekerApplications.put(jobSeekerName, savedJobApplications);
    }

    private void addJob(String employerName, String jobName, String jobType) {
        List<Job> savedJobs = employerJobs.getOrDefault(employerName, new ArrayList<>());
        Job job = new Job(jobName, jobType);
        savedJobs.add(job);
        employerJobs.put(employerName, savedJobs);
    }

    List<List<String>> getEmployerJobs(String employerName) {
        return employerJobs.get(employerName).stream()
                .map(job -> new ArrayList<String>() {{
                    add(job.getJobName());
                    add(job.getJobType());
                }}).collect(Collectors.toList());
    }

    List<JobApplication> getJobSeekerApplications_temp(String employerName) {
        return jobSeekerApplications.get(employerName);
    }

    public List<String> findApplicants(String jobName, String employerName) {
        return findApplicants(jobName, employerName, null);
    }

    public List<String> findApplicants(String jobName, String employerName, LocalDate from) {
        return findApplicants(jobName, employerName, from, null);
    }

    public List<String> findApplicants(String jobName, String employerName, LocalDate from, LocalDate to) {
        Predicate<List<String>> predicate = queryCondition(jobName, from, to);
        return getApplicants(predicate);
    }

    private Predicate<List<String>> queryCondition(String jobName, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return job -> job.get(0).equals(jobName);
        }
        if (jobName == null && to == null) {
            return job ->
                    !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (jobName == null && from == null) {
            return job ->
                    !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (jobName == null) {
            return job -> !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd"))) && !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (to != null) {
            return job -> job.get(0).equals(jobName) && !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return job -> job.get(0).equals(jobName) && !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private List<String> getApplicants(Predicate<List<String>> predicate) {
        return this.jobSeekerApplications.entrySet().stream().filter(set -> {
            List<JobApplication> jobs = set.getValue();
            return jobs.stream().map(job -> new ArrayList<String>() {{
                add(job.getJobName());
                add(job.getJobType());
                add(job.getApplicationTime());
                add(job.getEmployerName());
            }}).anyMatch(predicate);
        }).map(Entry::getKey).collect(Collectors.toList());
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
        for (Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
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
        for (Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
            String applicant = set.getKey();
            newResult.append(set.getValue().stream()
                    .filter(job -> job.getApplicationTime().equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .map(job -> job.getEmployerName() + "," + job.getJobName() + "," + job.getJobType() + "," + applicant + "," + job.getApplicationTime() + "\n")
                    .collect(Collectors.joining()));
        }
        return newResult.toString();
    }

    public int getSuccessfulApplications(String employerName, String jobName) {
        int newResult = 0;
        for (Entry<String, List<JobApplication>> set : this.jobSeekerApplications.entrySet()) {
            List<JobApplication> jobs = set.getValue();
            newResult += jobs.stream()
                    .map(job -> new ArrayList<String>() {{
                        add(job.getJobName());
                        add(job.getJobType());
                        add(job.getApplicationTime());
                        add(job.getEmployerName());
                    }})
                    .anyMatch(job -> job.get(3).equals(employerName) && job.get(0).equals(jobName)) ? 1 : 0;
        }

        return newResult;
    }

    public int getUnsuccessfulApplications(String employerName, String jobName) {
        return (int) failedApplications.stream().filter(job -> job.get(0).equals(jobName) && job.get(3).equals(employerName)).count();
    }
}

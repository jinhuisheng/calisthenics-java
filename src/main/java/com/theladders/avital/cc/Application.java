package com.theladders.avital.cc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Map.*;

public class Application {
    private HashMap<String, List<Job>> employerJobs = new HashMap<>();
    private HashMap<String, List<JobApplication>> jobSeekerApplications = new HashMap<>();
    private List<JobApplication> failedApplications = new ArrayList<>();
    private HashMap<String, List<Job>> seekerConcernJobs = new HashMap<>();
    private EmployerJobs employerJobs_temp = new EmployerJobs();


    public void applyJob(String employerName, String jobName, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime, JobType jobType) throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        checkJobTypeWhenApplyCommand(employerName, jobName, jobSeekerName, resumeApplicantName, applicationTime, jobType);
        addApply(employerName, jobName, jobType.getName(), jobSeekerName, applicationTime);
    }

    void publishJob(String employerName, String jobName, JobType jobType) throws NotSupportedJobTypeException {
        checkJobTypeWhenPublish(jobType);
//        addJob(employerName, jobName, jobType.getName());
        employerJobs_temp.addJob(employerName, jobName, jobType.getName());
    }

    private void checkJobTypeWhenApplyCommand(String employerName, String jobName, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime, JobType jobType) throws RequiresResumeForJReqJobException, InvalidResumeException {
        if (jobType.equals(JobType.JReq) && resumeApplicantName == null) {
            addFailedApplications(employerName, jobName, jobType.getName(), applicationTime);
            throw new RequiresResumeForJReqJobException();
        }

        if (jobType.equals(JobType.JReq) && !resumeApplicantName.equals(jobSeekerName)) {
            throw new InvalidResumeException();
        }
    }

    private void checkJobTypeWhenPublish(JobType jobType) throws NotSupportedJobTypeException {
        if (!jobType.equals(JobType.JReq) && !jobType.equals(JobType.ATS)) {
            throw new NotSupportedJobTypeException();
        }
    }

    private void addFailedApplications(String employerName, String jobName, String jobType, LocalDate applicationTime) {
        failedApplications.add(new JobApplication(jobName, jobType, applicationTime, employerName));
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

    public void saveSeekerConcernJob(String jobSeekerName, String jobName, String jobType) {
        List<Job> savedJobs = seekerConcernJobs.getOrDefault(jobSeekerName, new ArrayList<>());
        savedJobs.add(new Job(jobName, jobType));
        seekerConcernJobs.put(jobSeekerName, savedJobs);
    }


    List<Job> getEmployerJobs(String employerName) {
        return employerJobs_temp.getEmployerJobs(employerName);
//        return employerJobs.get(employerName);
    }

    List<JobApplication> getJobSeekerApplications(String employerName) {
        return jobSeekerApplications.get(employerName);
    }

    public List<String> findApplicants(String jobName, LocalDate from) {
        return findApplicants(jobName, from, null);
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
                .map(Entry::getKey)
                .collect(Collectors.toList());
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
                    .anyMatch(job -> job.getEmployerName().equals(employerName) && job.getJobName().equals(jobName)) ? 1 : 0;
        }

        return newResult;
    }

    public int getUnsuccessfulApplications(String employerName, String jobName) {
        return (int) failedApplications.stream()
                .filter(job -> job.getJobName().equals(jobName) && job.getEmployerName().equals(employerName))
                .count();
    }

    public List<Job> getSeekerConcernJobs(String jobSeekerName) {
        return seekerConcernJobs.get(jobSeekerName);
    }
}

package com.theladders.avital.cc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Map.*;

public class Application {
    private final HashMap<String, List<List<String>>> jobs = new HashMap<>();
    private final HashMap<String, List<List<String>>> applied = new HashMap<>();
    private final List<List<String>> failedApplications = new ArrayList<>();
    private HashMap<String, List<Job>> employerJobs = new HashMap<>();

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
        List<List<String>> saved = this.applied.getOrDefault(jobSeekerName, new ArrayList<>());

        saved.add(new ArrayList<String>() {{
            add(jobName);
            add(jobType);
            add(applicationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            add(employerName);
        }});
        applied.put(jobSeekerName, saved);
    }

    private void addJob(String employerName, String jobName, String jobType) {
//        List<List<String>> saved = jobs.getOrDefault(employerName, new ArrayList<>());
//

        List<Job> savedJobs = employerJobs.getOrDefault(employerName, new ArrayList<>());
        Job job = new Job(jobName, jobType);
        savedJobs.add(job);
        employerJobs.put(employerName, savedJobs);

//        saved.add(new ArrayList<String>() {{
//            add(jobName);
//            add(jobType);
//        }});
//        jobs.put(employerName, saved);
    }

    public List<List<String>> getJobs(String employerName, String type) {
        if (type.equals("applied")) {
            return applied.get(employerName);
        }
        List<List<String>> result = employerJobs.get(employerName).stream()
                .map(job -> new ArrayList() {{
                    add(job.getJobName());
                    add(job.getJobType());
                }}).collect(Collectors.toList());
        return result;
//        return jobs.get(employerName);
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
        List<String> result = new ArrayList<String>() {
        };
        for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
            String applicant = set.getKey();
            List<List<String>> jobs = set.getValue();
            boolean hasAppliedToThisJob = jobs.stream().anyMatch(predicate);
            if (hasAppliedToThisJob) {
                result.add(applicant);
            }
        }
        return result;
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
        StringBuilder content = new StringBuilder();
        for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
            String applicant = set.getKey();
            content.append(set.getValue().stream()
                    .filter(job -> job.get(2).equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .map(job -> "<tr>" + "<td>" + job.get(3) + "</td>" + "<td>" + job.get(0) + "</td>" + "<td>" + job.get(1) + "</td>" + "<td>" + applicant + "</td>" + "<td>" + job.get(2) + "</td>" + "</tr>")
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
                + content
                + "</tbody>"
                + "</table>"
                + "</body>"
                + "</html>";
    }

    private String exportCsv(LocalDate date) {
        StringBuilder result = new StringBuilder("Employer,Job,Job Type,Applicants,Date" + "\n");
        for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
            String applicant = set.getKey();
            result.append(set.getValue().stream()
                    .filter(job -> job.get(2).equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .map(job -> job.get(3) + "," + job.get(0) + "," + job.get(1) + "," + applicant + "," + job.get(2) + "\n")
                    .collect(Collectors.joining()));
        }
        return result.toString();
    }

    public int getSuccessfulApplications(String employerName, String jobName) {
        int result = 0;
        for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
            List<List<String>> jobs = set.getValue();
            result += jobs.stream().anyMatch(job -> job.get(3).equals(employerName) && job.get(0).equals(jobName)) ? 1 : 0;
        }
        return result;
    }

    public int getUnsuccessfulApplications(String employerName, String jobName) {
        return (int) failedApplications.stream().filter(job -> job.get(0).equals(jobName) && job.get(3).equals(employerName)).count();
    }
}

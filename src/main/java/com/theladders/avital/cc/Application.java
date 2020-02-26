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

    public void execute(String command, String employerName, String jobName, String jobType, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime) throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        if (command == "publish") {
            if (!jobType.equals("JReq") && !jobType.equals("ATS")) {
                throw new NotSupportedJobTypeException();
            }

            List<List<String>> alreadyPublished = jobs.getOrDefault(employerName, new ArrayList<>());

            alreadyPublished.add(new ArrayList<String>() {{
                add(jobName);
                add(jobType);
            }});
            jobs.put(employerName, alreadyPublished);
            return;
        }
        if (command == "save") {
            List<List<String>> saved = jobs.getOrDefault(employerName, new ArrayList<>());

            saved.add(new ArrayList<String>() {{
                add(jobName);
                add(jobType);
            }});
            jobs.put(employerName, saved);
        }
        if (command == "apply") {
            if (jobType.equals("JReq") && resumeApplicantName == null) {
                List<String> failedApplication = new ArrayList<String>() {{
                    add(jobName);
                    add(jobType);
                    add(applicationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    add(employerName);
                }};
                failedApplications.add(failedApplication);
                throw new RequiresResumeForJReqJobException();
            }

            if (jobType.equals("JReq") && !resumeApplicantName.equals(jobSeekerName)) {
                throw new InvalidResumeException();
            }
            List<List<String>> saved = this.applied.getOrDefault(jobSeekerName, new ArrayList<>());

            saved.add(new ArrayList<String>() {{
                add(jobName);
                add(jobType);
                add(applicationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                add(employerName);
            }});
            applied.put(jobSeekerName, saved);
        }
    }

    public List<List<String>> getJobs(String employerName, String type) {
        if (type.equals("applied")) {
            return applied.get(employerName);
        }

        return jobs.get(employerName);
    }

    public List<String> findApplicants(String jobName, String employerName) {
        return findApplicants(jobName, employerName, null);
    }

    public List<String> findApplicants(String jobName, String employerName, LocalDate from) {
        return findApplicants(jobName, employerName, from, null);
    }

    public List<String> findApplicants(String jobName, String employerName, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            List<String> result = new ArrayList<String>() {
            };
            for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
                String applicant = set.getKey();
                List<List<String>> jobs = set.getValue();
                Predicate<List<String>> predicate = job -> job.get(0).equals(jobName);
                boolean hasAppliedToThisJob = jobs.stream().anyMatch(predicate);
                if (hasAppliedToThisJob) {
                    result.add(applicant);
                }
            }
            return result;
        }
        if (jobName == null && to == null) {
            List<String> result = new ArrayList<String>() {
            };
            for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
                String applicant = set.getKey();
                List<List<String>> jobs = set.getValue();
                Predicate<List<String>> predicate = job ->
                        !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                boolean isAppliedThisDate = jobs.stream().anyMatch(predicate);
                if (isAppliedThisDate) {
                    result.add(applicant);
                }
            }
            return result;
        }
        if (jobName == null && from == null) {
            List<String> result = new ArrayList<String>() {
            };
            for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
                String applicant = set.getKey();
                List<List<String>> jobs = set.getValue();
                Predicate<List<String>> predicate = job ->
                        !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                boolean isAppliedThisDate = jobs.stream().anyMatch(predicate);
                if (isAppliedThisDate) {
                    result.add(applicant);
                }
            }
            return result;

        }
        if (jobName == null) {
            List<String> result = new ArrayList<String>() {
            };
            for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
                String applicant = set.getKey();
                List<List<String>> jobs = set.getValue();
                Predicate<List<String>> predicate = job -> !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd"))) && !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                boolean isAppliedThisDate = jobs.stream().anyMatch(predicate);
                if (isAppliedThisDate) {
                    result.add(applicant);
                }
            }
            return result;

        }
        if (to != null) {
            List<String> result = new ArrayList<String>() {
            };
            for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
                String applicant = set.getKey();
                List<List<String>> jobs = set.getValue();
                Predicate<List<String>> predicate = job -> job.get(0).equals(jobName) && !to.isBefore(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                boolean isAppliedThisDate = jobs.stream().anyMatch(predicate);
                if (isAppliedThisDate) {
                    result.add(applicant);
                }
            }
            return result;
        }
        List<String> result = new ArrayList<String>() {
        };
        for (Entry<String, List<List<String>>> set : this.applied.entrySet()) {
            String applicant = set.getKey();
            List<List<String>> jobs = set.getValue();
            Predicate<List<String>> predicate = job -> job.get(0).equals(jobName) && !from.isAfter(LocalDate.parse(job.get(2), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            boolean isAppliedThisDate = jobs.stream().anyMatch(predicate);
            if (isAppliedThisDate) {
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

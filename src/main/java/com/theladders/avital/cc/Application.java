package com.theladders.avital.cc;

import java.time.LocalDate;
import java.util.*;

/**
 * @author huisheng.jin
 */
public class Application {
    private JobSeekerApplications jobSeekerApplications = new JobSeekerApplications();
    private FailedApplications failedApplications = new FailedApplications();
    private SeekerConcernJobs seekerConcernJobs = new SeekerConcernJobs();
    private EmployerJobs employerJobs = new EmployerJobs();

    public void applyJob(String employerName, String jobName, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime, JobType jobType) throws RequiresResumeForJReqJobException, InvalidResumeException {
        checkJobTypeWhenApplyCommand(employerName, jobName, jobSeekerName, resumeApplicantName, applicationTime, jobType);
        jobSeekerApplications.addApply(employerName, jobName, jobType.getName(), jobSeekerName, applicationTime);
    }

    void publishJob(String employerName, String jobName, JobType jobType) throws NotSupportedJobTypeException {
        checkJobTypeWhenPublish(jobType);
        employerJobs.addJob(employerName, jobName, jobType.getName());
    }

    public void saveSeekerConcernJob(String jobSeekerName, String jobName, String jobType) {
        seekerConcernJobs.saveSeekerConcernJob(jobSeekerName, jobName, jobType);
    }

    List<Job> getEmployerJobs(String employerName) {
        return employerJobs.getEmployerJobs(employerName);
    }

    List<JobApplication> getJobSeekerApplications(String employerName) {
        return jobSeekerApplications.getJobSeekerApplications(employerName);
    }

    public List<String> findApplicants(String jobName, LocalDate from, LocalDate to) {
        return jobSeekerApplications.findApplicants(jobName, from, to);
    }

    public String export(String type, LocalDate date) {
        return jobSeekerApplications.export(type, date);
    }

    public int getSuccessfulApplications(String employerName, String jobName) {
        return jobSeekerApplications.getSuccessfulApplications(employerName, jobName);
    }

    public int getUnsuccessfulApplications(String employerName, String jobName) {
        return failedApplications.getUnsuccessfulApplications(employerName, jobName);
    }

    public List<Job> getSeekerConcernJobs(String jobSeekerName) {
        return seekerConcernJobs.getSeekerConcernJobs(jobSeekerName);
    }

    private void checkJobTypeWhenApplyCommand(String employerName, String jobName, String jobSeekerName, String resumeApplicantName, LocalDate applicationTime, JobType jobType) throws RequiresResumeForJReqJobException, InvalidResumeException {
        if (jobType.equals(JobType.JReq) && resumeApplicantName == null) {
            failedApplications.addFailedApplications(employerName, jobName, jobType.getName(), applicationTime);
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

}

package com.theladders.avital.cc;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApplicationTest {
    Application application;

    @Before
    public void setUp() throws Exception {
        application = new Application();
    }

    @Test
    public void employers_should_be_able_to_publish_a_job() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerName = "";
        String jobName = "高级前端开发";
        application.publishJob(employerName, jobName, "JReq");
        List<Job> jobs = application.getEmployerJobs(employerName);
        List<Job> expected = new ArrayList<Job>() {{
            add(new Job("高级前端开发", "JReq"));
        }};

        assertThat(jobs, is(expected));
    }

    @Test
    public void employers_should_only_be_able_to_see_jobs_published_by_them() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String employerTencent = "Tencent";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";
        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.publishJob(employerTencent, juniorJavaDevJob, "JReq");

        List<Job> jobs = application.getEmployerJobs(employerAlibaba);
        List<Job> expected = new ArrayList<Job>() {{
            add(new Job("高级Java开发", "JReq"));
        }};

        assertThat(jobs, is(expected));
    }

    @Test
    public void employers_should_be_able_to_publish_ATS_jobs() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");

        List<Job> jobs = application.getEmployerJobs(employerAlibaba);
        List<Job> expected = new ArrayList<Job>() {{
            add(new Job("高级Java开发", "ATS"));
        }};


        assertThat(jobs, is(expected));
    }

    @Test(expected = NotSupportedJobTypeException.class)
    public void employers_should_not_be_able_to_publish_jobs_that_are_neither_ATS_nor_JReq() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "RJeq");
    }

    @Test
    public void jobseekers_should_be_able_to_save_jobs_published_by_employers_for_later_review() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerName = "Jacky";
        String jobName = "高级Java开发";
        application.publishJob(employerAlibaba, jobName, "JReq");
        application.saveSeekerConcernJob(jobSeekerName, jobName, "JReq");

        List<Job> jobs = application.getSeekerConcernJobs(jobSeekerName);
        List<Job> expected = new ArrayList<Job>() {{
            add(new Job("高级Java开发", "JReq"));
        }};

        assertThat(jobs, is(expected));
    }

    @Test
    public void jobseekers_should_be_able_to_apply_for_an_ATS_job_some_employer_published_without_a_resume() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerName = "Jacky";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerName, null, LocalDate.parse("2020-01-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerName, null, LocalDate.parse("2020-01-01"), JobType.ATS);

        List<JobApplication> appliedJobs = application.getJobSeekerApplications(jobSeekerName);
        List<JobApplication> expected = new ArrayList<JobApplication>() {{
            add(new JobApplication("Java开发", "ATS", "2020-01-01", "Alibaba"));
            add(new JobApplication("高级Java开发", "ATS", "2020-01-01", "Alibaba"));
        }};
        assertThat(appliedJobs, is(expected));
    }

    @Test(expected = RequiresResumeForJReqJobException.class)
    public void jobseekers_should_not_be_able_to_apply_for_an_JReq_job_some_employer_published_without_a_resume() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerName = "Jacky";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerName, null, LocalDate.now(), JobType.JREQ);
    }

    @Test(expected = InvalidResumeException.class)
    public void jobseekers_should_not_be_able_to_apply_for_an_JReq_job_some_employer_published_with_someone_else_s_resume() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerName = "Jacky";
        String seniorJavaDevJob = "高级Java开发";
        String resumeApplicantName = "Jacky Chen";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerName, resumeApplicantName, LocalDate.now(), JobType.JREQ);
    }

    @Test
    public void employers_should_be_able_to_find_applicants_of_a_job() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerLam = "Lam";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.now(), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerLam, null, LocalDate.now(), JobType.ATS);
        List<String> applicants = application.findApplicants(seniorJavaDevJob, employerAlibaba);

        List<String> expected = new ArrayList<String>() {{
            add("Lam");
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_application_date() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        List<String> applicants = application.findApplicants(null, employerAlibaba, LocalDate.parse("1999-12-20"));

        List<String> expected = new ArrayList<String>() {{
            add("Ho");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_period_when_period_end_is_given_while_period_start_is_not() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        List<String> applicants = application.findApplicants(null, employerAlibaba, null, LocalDate.parse("1999-01-01"));

        List<String> expected = new ArrayList<String>() {{
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_period() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String seniorJavaDevJob = "高级Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        List<String> applicants = application.findApplicants(null, employerAlibaba, LocalDate.parse("1997-07-01"), LocalDate.parse("1999-12-20"));

        List<String> expected = new ArrayList<String>() {{
            add("Ho");
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_job_name_and_period_when_period_start_is_given_while_period_end_is_not() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String resumeApplicantName = "Jacky";
        String jobSeekerHo = "Ho";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, resumeApplicantName, LocalDate.parse("1999-12-20"), JobType.JREQ);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);

        List<String> applicants = application.findApplicants(seniorJavaDevJob, employerAlibaba, LocalDate.parse("1999-12-20"));

        List<String> expected = new ArrayList<String>() {{
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_job_name_and_period_when_period_end_is_given_while_period_start_is_not() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);

        List<String> applicants = application.findApplicants(juniorJavaDevJob, employerAlibaba, null, LocalDate.parse("1999-01-01"));

        List<String> expected = new ArrayList<String>() {{
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void employers_should_be_able_to_find_applicants_to_a_job_by_job_name_and_period() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerWong = "Wong";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String jobSeekerLam = "Lam";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerWong, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1998-01-01"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerLam, null, LocalDate.parse("1999-12-20"), JobType.ATS);

        List<String> applicants = application.findApplicants(juniorJavaDevJob, employerAlibaba, LocalDate.parse("1997-01-01"), LocalDate.parse("1999-01-01"));

        List<String> expected = new ArrayList<String>() {{
            add("Ho");
            add("Jacky");
        }};

        assertThat(applicants, is(expected));
    }

    @Test
    public void should_generator_csv_reports_of_all_jobseekers_on_a_given_date() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jackyResume = "Jacky";
        String jobSeekerHo = "Ho";
        String jobSeekerLam = "Lam";
        String lamResume = "Lam";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, jackyResume, LocalDate.parse("1999-12-20"), JobType.JREQ);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerLam, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerLam, lamResume, LocalDate.parse("1999-12-20"), JobType.JREQ);

        String csv = application.export("csv", LocalDate.parse("1999-12-20"));
        String expected = "Employer,Job,Job Type,Applicants,Date" + "\n" + "Alibaba,Java开发,ATS,Ho,1999-12-20" + "\n" + "Alibaba,Java开发,ATS,Lam,1999-12-20" + "\n" + "Alibaba,高级Java开发,JReq,Lam,1999-12-20" + "\n" + "Alibaba,高级Java开发,JReq,Jacky,1999-12-20" + "\n";

        assertThat(csv, is(expected));
    }

    @Test
    public void should_generator_html_reports_of_all_jobseekers_on_a_given_date() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jackyResume = "Jacky";
        String jobSeekerHo = "Ho";
        String jobSeekerLam = "Lam";
        String lamResume = "Lam";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerJacky, null, LocalDate.parse("1997-07-01"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, jackyResume, LocalDate.parse("1999-12-20"), JobType.JREQ);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerLam, null, LocalDate.parse("1999-12-20"), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerLam, lamResume, LocalDate.parse("1999-12-20"), JobType.JREQ);

        String csv = application.export("html", LocalDate.parse("1999-12-20"));
        String expected = "<!DOCTYPE html>"
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
                + "<tr>"
                + "<td>Alibaba</td>"
                + "<td>Java开发</td>"
                + "<td>ATS</td>"
                + "<td>Ho</td>"
                + "<td>1999-12-20</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Alibaba</td>"
                + "<td>Java开发</td>"
                + "<td>ATS</td>"
                + "<td>Lam</td>"
                + "<td>1999-12-20</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Alibaba</td>"
                + "<td>高级Java开发</td>"
                + "<td>JReq</td>"
                + "<td>Lam</td>"
                + "<td>1999-12-20</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Alibaba</td>"
                + "<td>高级Java开发</td>"
                + "<td>JReq</td>"
                + "<td>Jacky</td>"
                + "<td>1999-12-20</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>"
                + "</body>"
                + "</html>";

        assertThat(csv, is(expected));
    }

    @Test
    public void should_be_able_to_see_successful_application_of_a_job_for_an_employer() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String employerTencent = "Tencent";
        String jobSeekerJacky = "Jacky";
        String jobSeekerHo = "Ho";
        String jobSeekerLam = "Lam";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "ATS");
        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        application.publishJob(employerTencent, juniorJavaDevJob, "ATS");
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.now(), JobType.ATS);
        application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerLam, null, LocalDate.now(), JobType.ATS);
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerHo, null, LocalDate.now(), JobType.ATS);
        application.applyJob(employerTencent, juniorJavaDevJob, jobSeekerHo, null, LocalDate.now(), JobType.ATS);

        assertThat(application.getSuccessfulApplications(employerAlibaba, seniorJavaDevJob), is(2));
        assertThat(application.getSuccessfulApplications(employerAlibaba, juniorJavaDevJob), is(1));
    }

    @Test
    public void should_be_able_to_see_unsuccessful_applications_of_a_job_for_an_employer() throws NotSupportedJobTypeException, RequiresResumeForJReqJobException, InvalidResumeException {
        String employerAlibaba = "Alibaba";
        String jobSeekerJacky = "Jacky";
        String jobSeekerLam = "Lam";
        String seniorJavaDevJob = "高级Java开发";
        String juniorJavaDevJob = "Java开发";

        application.publishJob(employerAlibaba, seniorJavaDevJob, "JReq");
        application.publishJob(employerAlibaba, juniorJavaDevJob, "ATS");
        try {
            application.applyJob(employerAlibaba, seniorJavaDevJob, jobSeekerJacky, null, LocalDate.now(), JobType.JREQ);
        } catch (RequiresResumeForJReqJobException ignored) {
        }
        application.applyJob(employerAlibaba, juniorJavaDevJob, jobSeekerLam, null, LocalDate.now(), JobType.ATS);

        assertThat(application.getUnsuccessfulApplications(employerAlibaba, seniorJavaDevJob), is(1));
        assertThat(application.getUnsuccessfulApplications(employerAlibaba, juniorJavaDevJob), is(0));
    }
}

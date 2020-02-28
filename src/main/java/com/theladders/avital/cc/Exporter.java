package com.theladders.avital.cc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huisheng.jin
 * @date 2020/2/28.
 */
public class Exporter {
    public static String export(String type, Map<String, List<JobApplication>> exportData) {
        if ("csv".equals(type)) {
            return exportCsv(exportData);
        } else {
            return exportHtml(exportData);
        }
    }
    private static String exportHtml(Map<String, List<JobApplication>> exportData) {
        StringBuilder newContent = new StringBuilder();
        for (Map.Entry<String, List<JobApplication>> set : exportData.entrySet()) {
            String applicant = set.getKey();
            newContent.append(set.getValue().stream()
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

    private static String exportCsv(Map<String, List<JobApplication>> exportData) {
        StringBuilder newResult = new StringBuilder("Employer,Job,Job Type,Applicants,Date" + "\n");
        for (Map.Entry<String, List<JobApplication>> set : exportData.entrySet()) {
            String applicant = set.getKey();
            newResult.append(set.getValue().stream()
                    .map(job -> job.getEmployerName() + "," + job.getJobName() + "," + job.getJobType() + "," + applicant + "," + job.getApplicationTime() + "\n")
                    .collect(Collectors.joining()));
        }
        return newResult.toString();
    }
}

package com.digitalforensics.model;

/**
 * Any object that can appear as a row in a CSV report.
 * ReportService works with List<? extends Reportable>, so the same export
 * code handles evidence, custody records, timeline events and findings.
 */
public interface Reportable {

    String[] csvHeader();

    String[] toCsvRow();
}

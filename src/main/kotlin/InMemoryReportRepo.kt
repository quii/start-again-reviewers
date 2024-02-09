class InMemoryReportRepo : ReportRepo {
    private val reports: MutableList<Report> = mutableListOf()
    override fun add(report: Report) {
        reports.add(report)
    }
    override fun get(submission: Submission) = reports.filter { it.submission==submission }
}
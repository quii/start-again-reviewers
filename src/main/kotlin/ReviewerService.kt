data class Submission(val id: String)

data class Reviewer(val email: String)
data class Report(val submission: Submission, val reviewer: Reviewer, val content: String)

class ReviewerService{
    private val submissionsUnderReview: MutableMap<Submission, List<Reviewer>> = mutableMapOf()
    private val reports: MutableList<Report> = mutableListOf()

    fun review(submission: Submission, shortList: List<Reviewer>) {
        submissionsUnderReview[submission] = shortList
    }

    fun saveReport(report: Report) {
        if(!getShortList(report.submission).contains(report.reviewer)) {
            throw UnauthorizedReportException(report.reviewer, report.submission)
        }

        reports.add(report)
    }

    fun unInvite(submission: Submission, reviewer: Reviewer) {
        val shortList: MutableList<Reviewer> = getShortList(submission).toMutableList()
        shortList.remove(reviewer)
        submissionsUnderReview[submission] = shortList
    }

    fun getReports(submission: Submission): List<Report> {
        return reports.filter { it.submission==submission }
    }

    fun getShortList(submission: Submission): List<Reviewer> {
        return submissionsUnderReview[submission] ?: throw SubmissionDoesntExistException(submission)
    }

    fun closeReview(submission: Submission) {
        submissionsUnderReview.remove(submission)
    }
}
interface SubmissionReviewRepo {
    fun startReview(submission: Submission, shortList: List<Reviewer>)
    fun getShortList(submission: Submission): List<Reviewer>
    fun removeReviewer(submission: Submission, reviewer: Reviewer)
    fun close(submission: Submission)
    fun reviewerIsAuthorizedToReport(report: Report) = getShortList(report.submission).contains(report.reviewer)
}

interface ReportRepo {
    fun add(report: Report)
    fun get(submission: Submission): List<Report>
}

class ReviewerService(
    private val submissionReviewRepo: SubmissionReviewRepo,
    private val reports: ReportRepo
) {

    fun review(submission: Submission, shortList: List<Reviewer>) {
        submissionReviewRepo.startReview(submission, shortList)
    }

    fun closeReview(submission: Submission) {
        submissionReviewRepo.close(submission)
    }

    fun unInvite(submission: Submission, reviewer: Reviewer) {
        submissionReviewRepo.removeReviewer(submission, reviewer)
    }

    fun getShortList(submission: Submission) = submissionReviewRepo.getShortList(submission)

    fun saveReport(report: Report) {
        if (!submissionReviewRepo.reviewerIsAuthorizedToReport(report)) {
            throw UnauthorizedReportException(report.reviewer, report.submission)
        }
        reports.add(report)
    }

    fun getReports(submission: Submission): List<Report> = reports.get(submission)
}
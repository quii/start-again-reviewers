interface SubmissionReviewRepo {
    fun startReview(submission: Submission, shortList: List<Reviewer>)
    fun getShortList(submission: Submission): List<Reviewer>
    fun removeReviewer(submission: Submission, reviewer: Reviewer)
    fun close(submission: Submission)
    fun reviewerIsAuthorizedToReport(report: Report) = getShortList(report.submission).contains(report.reviewer)
    fun getSubmissions(reviewer: Reviewer): List<Submission>
}

interface ReportRepo {
    fun add(report: Report)
    fun get(submission: Submission): List<Report>
}

interface Notifier {
    fun notify(notification: Notification)
}

class ReviewerService(
    private val submissionReviewRepo: SubmissionReviewRepo,
    private val reports: ReportRepo,
    private val notifier: Notifier
) {

    fun review(submission: Submission, shortList: List<Reviewer>) {
        submissionReviewRepo.startReview(submission, shortList)
        shortList.forEach { reviewer -> notifier.notify(Notification(reviewer, submission, "plz review")) }
    }

    fun closeReview(submission: Submission) {
        val reviewersWhoHaveSentReports = reports.get(submission).map { report -> report.reviewer }.toSet()
        val shortList = submissionReviewRepo.getShortList(submission).toSet()

        shortList.subtract(reviewersWhoHaveSentReports).forEach{reviewer ->
            notifier.notify(Notification(reviewer, submission, "dont bother mate"))
        }

        submissionReviewRepo.close(submission)
    }

    fun getWorkload(reviewer: Reviewer): List<Submission> {
        return submissionReviewRepo.getSubmissions(reviewer)
    }

    fun unInvite(submission: Submission, reviewer: Reviewer) {
        submissionReviewRepo.removeReviewer(submission, reviewer)
        notifier.notify(Notification(reviewer, submission, "You suck i don't want to hear from you"))
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
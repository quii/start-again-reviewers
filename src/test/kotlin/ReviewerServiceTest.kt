import SomeReviewers.bernardoTheReviewer
import SomeReviewers.chrisTheReviewer
import SomeReviewers.edTheReviewer
import org.junit.jupiter.api.Test
import strikt.api.*
import strikt.assertions.*

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

class ReviewerServiceTest {
    private val sut = ReviewerService()

    @Test
    fun `an editor can give a shortlist for a submission to the reviewer service to get some reports`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val aSubmission = Submission("some id")

        sut.review(aSubmission, shortList)

        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        sut.saveReport(chrisReport)

        val reports = sut.getReports(aSubmission)
        expectThat(reports).hasSize(1).contains(chrisReport)
    }

    @Test
    fun `reviewers who were not shortlisted cannot review`() {
        val shortList = listOf(edTheReviewer, bernardoTheReviewer)
        val aSubmission = Submission("some id")

        sut.review(aSubmission, shortList)

        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        expectCatching { sut.saveReport(chrisReport) }.isFailure().isA<UnauthorizedReportException>()
    }

    @Test
    fun `cant send reports to a submission that does not exist`() {
        val chrisReport = Report(Submission("hasnt been opened for review"), chrisTheReviewer, "This paper sucks")
        expectCatching { sut.saveReport(chrisReport) }.isFailure().isA<SubmissionDoesntExistException>()
    }

    @Test
    fun `once a decision is made, review is closed, and reports will not be accepted`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val aSubmission = Submission("some id")
        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        val edReport = Report(aSubmission, edTheReviewer, "This paper is amazing")

        sut.review(aSubmission, shortList)
        sut.saveReport(chrisReport)
        sut.closeReview(aSubmission)

        expectCatching { sut.saveReport(edReport) }.isFailure().isA<SubmissionDoesntExistException>()

        val reports = sut.getReports(aSubmission)
        expectThat(reports).hasSize(1).contains(chrisReport)
    }

    @Test
    fun `can remove from the shortlist, which would prevent reports from that reviewer`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val aSubmission = Submission("some id")

        sut.review(aSubmission, shortList)
        sut.unInvite(aSubmission, chrisTheReviewer)

        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        expectCatching { sut.saveReport(chrisReport) }.isFailure().isA<UnauthorizedReportException>()
    }

    @Test
    fun `during review, i can see who is invited`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val aSubmission = Submission("some id")
        sut.review(aSubmission, shortList)
        expectThat(sut.getShortList(aSubmission)).contains(shortList)
    }

    @Test
    fun `once a decision is made, reviewers who have not sent a report, will be notified`() {

    }


}

object SomeReviewers {
    val chrisTheReviewer = Reviewer("chris.james@sn.com")
    val edTheReviewer = Reviewer("ed@pond.com")
    val bernardoTheReviewer = Reviewer("bern@rd.o")
}
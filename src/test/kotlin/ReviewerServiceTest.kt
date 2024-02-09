import SomeReviewers.bernardoTheReviewer
import SomeReviewers.chrisTheReviewer
import SomeReviewers.edTheReviewer
import org.junit.jupiter.api.Test
import strikt.api.*
import strikt.assertions.*

data class Submission(val id: String)

data class Reviewer(val email: String)

data class Report(val submission: Submission, val reviewer: Reviewer, val content: String)

class ReviewerService{
    val submissionsUnderReview: MutableMap<Submission, List<Reviewer>> = mutableMapOf()
    val reports: MutableList<Report> = mutableListOf()

    fun review(submission: Submission, shortList: List<Reviewer>) {
        submissionsUnderReview.set(submission, shortList)
    }

    fun saveReport(report: Report) {
        reports.add(report)
    }

    fun getReports(submission: Submission): List<Report> {
        return reports.filter { it.submission==submission }
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

    }

    @Test
    fun `once a decision is made, review is closed, and reports will not be accepted`() {

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
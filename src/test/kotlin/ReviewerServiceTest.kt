import SomeReviewers.bernardoTheReviewer
import SomeReviewers.chrisTheReviewer
import SomeReviewers.edTheReviewer
import org.junit.jupiter.api.Test
import strikt.api.*
import strikt.assertions.*

class ReviewerServiceTest {
    /*
    Should the magic box do notifications?

    I used to think no, but in this contrived example it's not so bad. If we keep it to "notifications", e.g simple text messages with call to actions
    is it really advantageous to move it out to a consumer's responsibility? For instance, once you call `sut.close`, right now it'll just do it for you, notify them. If we want revex
    to do it, we'd have to return a list of reviewers who _didnt_ send a report, and then they'd have to do it. Seems pointlessly chatty to me?
     */
    private val fakeNotifier = InMemoryNotifier()

    private val sut = ReviewerService(InMemorySubmissionReviewRepo(), InMemoryReportRepo(), fakeNotifier)
    private val aSubmission = Submission("some id")

    @Test
    fun `an editor can give a shortlist for a submission to the reviewer service to get some reports, the reviewers will be notified`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")

        sut.review(aSubmission, shortList)

        expectThat(fakeNotifier.notifications).contains(
            Notification(chrisTheReviewer, aSubmission, "plz review"),
            Notification(edTheReviewer, aSubmission, "plz review"),
            Notification(bernardoTheReviewer, aSubmission, "plz review")
        )

        sut.saveReport(chrisReport)

        val reports = sut.getReports(aSubmission)
        expectThat(reports).hasSize(1).contains(chrisReport)
    }

    @Test
    fun `once a decision is made, review is closed, and reports will not be accepted`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        val edReport = Report(aSubmission, edTheReviewer, "This paper is amazing")

        sut.review(aSubmission, shortList)
        sut.saveReport(chrisReport)
        sut.closeReview(aSubmission)

        expectThat(fakeNotifier.notifications).contains(
            Notification(edTheReviewer, aSubmission, "dont bother mate"),
            Notification(bernardoTheReviewer, aSubmission, "dont bother mate")
        ).not().contains(
            Notification(chrisTheReviewer, aSubmission, "dont bother mate")
        )

        expectCatching { sut.saveReport(edReport) }.isFailure().isA<SubmissionDoesntExistException>()

        val reports = sut.getReports(aSubmission)
        expectThat(reports).hasSize(1).contains(chrisReport)
    }

    @Test
    fun `reviewers who were not shortlisted cannot review`() {
        val shortList = listOf(edTheReviewer, bernardoTheReviewer)

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
    fun `can remove from the shortlist, which would prevent reports from that reviewer, and notify them`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)

        sut.review(aSubmission, shortList)
        sut.unInvite(aSubmission, chrisTheReviewer)

        expectThat(fakeNotifier.notifications).contains(
            Notification(chrisTheReviewer, aSubmission, "You suck i don't want to hear from you"),
        )

        val chrisReport = Report(aSubmission, chrisTheReviewer, "This paper sucks")
        expectCatching { sut.saveReport(chrisReport) }.isFailure().isA<UnauthorizedReportException>()
    }

    @Test
    fun `during review, i can see who is invited`() {
        val shortList = listOf(chrisTheReviewer, edTheReviewer, bernardoTheReviewer)
        sut.review(aSubmission, shortList)
        expectThat(sut.getShortList(aSubmission)).contains(shortList)
    }

}

object SomeReviewers {
    val chrisTheReviewer = Reviewer("chris.james@sn.com")
    val edTheReviewer = Reviewer("ed@pond.com")
    val bernardoTheReviewer = Reviewer("bern@rd.o")
}
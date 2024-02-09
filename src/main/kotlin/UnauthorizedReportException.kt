class UnauthorizedReportException(reviewer: Reviewer, submission: Submission) : Exception("$reviewer is not allowed to review $submission")
class SubmissionDoesntExistException(submission: Submission) : Exception("$submission does not exist")
class SubmissionClosedForReportsException(submission: Submission) : Exception("$submission no longer needs reports")

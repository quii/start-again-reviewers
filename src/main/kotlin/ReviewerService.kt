data class Submission(val id: String)

data class Reviewer(val email: String)
data class Report(val submission: Submission, val reviewer: Reviewer, val content: String)


class InMemorySubmissionReviewRepo : SubmissionReviewRepo {
    private val submissionsUnderReview: MutableMap<Submission, List<Reviewer>> = mutableMapOf()

    override fun startReview(submission: Submission, shortList: List<Reviewer>) {
        submissionsUnderReview[submission] = shortList
    }
    override fun getShortList(submission: Submission): List<Reviewer> {
        return submissionsUnderReview[submission] ?: throw SubmissionDoesntExistException(submission)
    }

    override fun removeReviewer(submission: Submission, reviewer: Reviewer) {
        val shortList: MutableList<Reviewer> = getShortList(submission).toMutableList()
        shortList.remove(reviewer)
        submissionsUnderReview[submission] = shortList
    }

    override fun close(submission: Submission) {
        submissionsUnderReview.remove(submission)
    }

    override fun getSubmissions(reviewer: Reviewer): List<Submission> {
        val filter = submissionsUnderReview.filter { entry: Map.Entry<Submission, List<Reviewer>> ->
            entry.value.contains(reviewer)
        }
        return filter.keys.toList()
    }
}
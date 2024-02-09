class InMemoryNotifier: Notifier {
    val notifications = mutableListOf<Notification>()

    override fun notify(notification: Notification) {
        notifications.add(notification)
    }
}
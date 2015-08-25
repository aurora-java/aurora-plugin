package aurora.plugin.redis;

public interface ISubscriber {

	/**
	 * Called when new message arrive
	 */
	public void onMessage(String channel, String message);

	public void onSubscribe(String channel, int subscribedChannels);

	public void onUnsubscribe(String channel, int subscribedChannels);

}

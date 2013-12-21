import java.util.Properties;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Collection;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.Colors;

import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;
import twitter4j.auth.AccessToken;

public class TwitterListener extends ListenerAdapter<PircBotX> {
	private HashMap<String, Long> usersToFollow;
	private PircBotX bot;
	private String channel;
	private StatusListener listener;
	private RequestToken requestToken;
	private boolean oauthInit = false;
	
	private String consumerKey = "aKiSjO4bhl3cXJLgpDpaA";
	private String consumerSecret = "m53AvFNgNE6naL8A2Mcytzxc9toigGYyPVfrBneck";

	public TwitterListener(final PircBotX bot, final String channel, Properties props) {
		this.bot = bot;
		this.channel = channel;
		usersToFollow = new HashMap<String, Long>();
		this.listener = new StatusListener() {
			private TwitterListener parent;
			private StatusListener init(TwitterListener parent) {
				this.parent = parent;
				return this;
			}
			
			@Override
			public void onException(Exception arg0) {
				System.err.println(arg0.getMessage());
				arg0.printStackTrace();
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
			}

			@Override
			public void onStatus(Status status) {
				User u = status.getUser();
				if (parent.usersToFollow.containsValue(u.getId())) {
					bot.sendMessage(channel, Colors.BOLD + "[TWITTER] @" + status.getUser().getScreenName() + ": " + status.getText());
				}
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
			}
		}.init(this);
		TwitterStream ts = TwitterStreamFactory.getSingleton();
		ts.addListener(this.listener);
	}

	public void onMessage(MessageEvent event) {
		String command = event.getMessage().toLowerCase();
		Scanner scanner = new Scanner(command);

		if (scanner.hasNext("!twitter")) {
			scanner.next();
			if (scanner.hasNext("add")) {
				this.checkTwitterOauthNeeded();
				if (!this.oauthInit) {
					return;
				}
				scanner.next();
				if (scanner.hasNext()) {
					String name = scanner.next();
					this.addTwitterUser(name);
				} else {
					bot.sendMessage(channel, "Twitter name not specified");
				}
			} else if (scanner.hasNext("remove")) {
				this.checkTwitterOauthNeeded();
				if (!this.oauthInit) {
					return;
				}
				scanner.next();
				if (scanner.hasNext()) {
					String name = scanner.next();
					if (this.usersToFollow.containsKey(name)) {
						this.removeTwitterUser(name);
					} else {
						bot.sendMessage(channel, name + " not found");
					}
				} else {
					bot.sendMessage(channel, "Twitter name not specified");
				}
			} else if (scanner.hasNext("purge")) {
				this.checkTwitterOauthNeeded();
				if (!this.oauthInit) {
					return;
				}
				this.usersToFollow.clear();
				bot.sendMessage(channel, "Purged all twitter users");
				setFilter();
			} else if (scanner.hasNext("oauth")) {
				scanner.next();
				if (scanner.hasNext()) {
					String pin = scanner.next();
					this.finalizeTwitterOauth(pin);
				} else {
					if (!this.oauthInit) {
						this.checkTwitterOauthNeeded();
					} else {
						bot.sendMessage(channel, "Oauth settings present");
					}
				}
			} else {
				bot.sendMessage(channel, "Valid commands are add, remove, purge and oauth.");
			}
		}
	}
	
	private void finalizeTwitterOauth(String pin) {
		AccessToken accessToken = null;
		Twitter tw = TwitterFactory.getSingleton();
		try {
			accessToken = tw.getOAuthAccessToken(this.requestToken, pin);
		} catch (TwitterException te) {
			te.printStackTrace();
			throw new RuntimeException(te);
		}
		
		Properties newProps = new Properties();
		newProps.setProperty("debug", "true");
		newProps.setProperty("oauth.consumerKey", this.consumerKey);
		newProps.setProperty("oauth.consumerSecret", this.consumerSecret);
		newProps.setProperty("oauth.accessToken", accessToken.getToken());
		newProps.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());
		try {
			FileOutputStream fos = new FileOutputStream("twitter4j.properties");
			newProps.store(fos, null);
		} catch (IOException ioe) {
			System.out.println("Failed to write twitter properties to disk");
			ioe.printStackTrace();
			System.exit(1);
		}
		bot.sendMessage(channel, "Oauth successful.  Please restart the bot.");
		System.exit(0);
	}
	
	private void getTwitterOauthRequestToken() {
		Twitter tw = TwitterFactory.getSingleton();
		if (this.requestToken == null) {
			tw.setOAuthConsumer(this.consumerKey, this.consumerSecret);
			try {
				this.requestToken = tw.getOAuthRequestToken();
			} catch (TwitterException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		bot.sendMessage(channel, "OAuth needed: " + requestToken.getAuthorizationURL());
		bot.sendMessage(channel, "Use \"!twitter oauth [PIN]\"");
		return;
	}
	
	private void checkTwitterOauthNeeded() {
		if (!this.oauthInit) {
			Properties twitterProps = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("twitter4j.properties");
				this.oauthInit = true;
			} catch (IOException ioe) {
				System.out.println("twitter4j.properties not found");
				this.getTwitterOauthRequestToken();
			}
		}
	}
	
	private void addTwitterUser(String user) {
		Twitter tw = TwitterFactory.getSingleton();
		ResponseList<User> userIdents;
		String[] users = new String[1];
		users[0] = user;
		try {
			userIdents = tw.lookupUsers(users);
			this.usersToFollow.put(user, userIdents.get(0).getId());
			setFilter();
			bot.sendMessage(channel, user + " added.");
		} catch (TwitterException e) {
			bot.sendMessage(channel, "User " + user + " not found.");
		}
	}
	
	private void removeTwitterUser(String user) {
		if (this.usersToFollow.containsKey(user)) {
			this.usersToFollow.remove(user);
			setFilter();
			bot.sendMessage(channel, user + " removed.");
		} else {
			bot.sendMessage(channel, "User " + user + " not found.");
		}
	}
	
	private void setFilter() {
		if (!this.oauthInit) {
			return;
		}
		
		Twitter tw = TwitterFactory.getSingleton();
		TwitterStream ts = TwitterStreamFactory.getSingleton();
		
		if (usersToFollow.size() <= 0) {
			ts.shutdown();
			return;
		}
		

		Collection<Long> longs = this.usersToFollow.values();
		Long[] followArray = longs.toArray(new Long[0]);
		long[] converted = new long[followArray.length];
		for (int i = 0; i < followArray.length; i++) {
			converted[i] = (long)followArray[i];
		}
		
		ts.filter(new FilterQuery(0, converted));
	}

}

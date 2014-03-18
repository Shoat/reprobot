import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class ReproBot extends ListenerAdapter {
	
	public static void main(String[] args) throws Exception {
		
		//instantiate underlying bot
		PircBotX destBot = new PircBotX();

		//load properties from disk
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("reprobot.properties");
		} catch (IOException ioe) {
			System.out.println("Can't find reprobot.proprties in local directory.");
			System.out.println("Wrting out example file and terminating.");
			System.out.println("Modify this file and re-run.");
			
			try {
				props.setProperty("dest_irc_server", "irc.slashnet.org");
				props.setProperty("dest_irc_channel", "#reprobotdest");
				props.setProperty("dest_bot_nick", "reprobot2");
				props.setProperty("dest_nick_pass", "");
				props.setProperty("ownernick", "maradine");
				props.setProperty("twitter_bootstrap", "user1,user2");
		
				props.store(new FileOutputStream("reprobot.properties"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		props.load(fis);
		if (!props.containsKey("dest_irc_server") || !props.containsKey("dest_irc_channel") ||
				!props.containsKey("dest_bot_nick")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String destIrcServer = props.getProperty("dest_irc_server");
		String destIrcChannel = props.getProperty("dest_irc_channel");
		String destBotNick = props.getProperty("dest_bot_nick");
		String ownernick = props.getProperty("ownernick");
		
		//seed the permissions manager
		PermissionsManager pm = PermissionsManager.initInstance(ownernick);
		
		//add misc listeners.  only dest bot gets command handlers
		destBot.getListenerManager().addListener(new PermissionsHandler());

		//set up twitter listener
		destBot.getListenerManager().addListener(new TwitterListener(destBot, destIrcChannel, props));

		//connect
		destBot.setVerbose(true);
		destBot.setName(destBotNick);
		destBot.connect(destIrcServer);
		
		//identify with nickserv if so enabled
		String destNickPass = props.getProperty("dest_nick_pass");
		if (destNickPass != null) {
			if (!destNickPass.isEmpty()) {
				destBot.identify(destNickPass);
			}
		}


		//join channel
		destBot.joinChannel(destIrcChannel);
		
		//pause to let channel join complete.  If we failed, exit.	
		Thread.sleep(5000);
		boolean killit = false;
		if (!destBot.channelExists(destIrcChannel)) {
			System.out.println("*** Bot failed to connect to channel \""+destIrcChannel+"\".  Either we got shunted, or the server is experiencing unusual load.");
			killit = true;
		}
		if (killit) {
			System.exit(1);
		}

		
	}
}


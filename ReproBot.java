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
		PircBotX sourceBot = new PircBotX();
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
				props.setProperty("source_irc_server", "irc.slashnet.org");
				props.setProperty("source_irc_channel", "#reprobotsource");
				props.setProperty("dest_irc_server", "irc.slashnet.org");
				props.setProperty("dest_irc_channel", "#reprobotdest");
				props.setProperty("source_bot_nick", "reprobot");
				props.setProperty("dest_bot_nick", "reprobot2");
				props.setProperty("source_nick_pass", "");
				props.setProperty("dest_nick_pass", "");
				props.setProperty("ownernick", "maradine");
		
				props.store(new FileOutputStream("reprobot.properties"), null);
			} catch (IOException ioe2) {
				System.out.println("There was an error writing to the filesystem.");
			}
			System.exit(1);

		} 			
		props.load(fis);
		if (!props.containsKey("source_irc_server") || !props.containsKey("source_irc_channel") ||
				!props.containsKey("dest_irc_server") || !props.containsKey("dest_irc_channel") ||
				!props.containsKey("source_bot_nick") || !props.containsKey("dest_bot_nick")) {
			System.out.println("Config file is incomplete.  Delete it to receive a working template.");
			System.exit(1);
		}
		String sourceIrcServer = props.getProperty("source_irc_server");
		String sourceIrcChannel = props.getProperty("source_irc_channel");
		String destIrcServer = props.getProperty("dest_irc_server");
		String destIrcChannel = props.getProperty("dest_irc_channel");
		String sourceBotNick = props.getProperty("source_bot_nick");
		String destBotNick = props.getProperty("dest_bot_nick");
		String ownernick = props.getProperty("ownernick");
		
		//seed the permissions manager
		PermissionsManager pm = PermissionsManager.initInstance(ownernick);
		
		//add misc listeners.  only dest bot gets command handlers
		destBot.getListenerManager().addListener(new BanterBox());
		destBot.getListenerManager().addListener(new PermissionsHandler());
		sourceBot.getListenerManager().addListener(new GeneralHandler(sourceBot, destBot, props));

		//connect
		sourceBot.setVerbose(true);
		destBot.setVerbose(true);
		sourceBot.setName(sourceBotNick);
		destBot.setName(destBotNick);
		sourceBot.connect(sourceIrcServer);
		destBot.connect(destIrcServer);
		
		//identify with nickserv if so enabled
		String sourceNickPass = props.getProperty("source_nick_pass");
		String destNickPass = props.getProperty("dest_nick_pass");
		if (sourceNickPass != null) {
			if (!sourceNickPass.isEmpty()) {
				sourceBot.identify(sourceNickPass);
			}
		}
		if (destNickPass != null) {
			if (!destNickPass.isEmpty()) {
				destBot.identify(destNickPass);
			}
		}


		//join channels
		sourceBot.joinChannel(sourceIrcChannel);
		destBot.joinChannel(destIrcChannel);
		
		//pause to let channel join complete.  If we failed, exit.	
		Thread.sleep(5000);
		boolean killit = false;
		if (!sourceBot.channelExists(sourceIrcChannel)) {
			System.out.println("*** Bot failed to connect to channel \""+sourceIrcChannel+"\".  Either we got shunted,  or the server is experiencing unusual load.");
			killit = true;
		}
		if (!destBot.channelExists(destIrcChannel)) {
			System.out.println("*** Bot failed to connect to channel \""+destIrcChannel+"\".  Either we got shunted, or the server is experiencing unusual load.");
			killit = true;
		}
		if (killit) {
			System.exit(1);
		}

		
	}
}


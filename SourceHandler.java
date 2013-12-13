import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import org.pircbotx.Colors;
import java.util.Properties;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class SourceHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX sourceBot;
	private PircBotX destBot;
	private LinkedList<String> celebList;
	private Properties props;
	private HashMap<String, HashMap<String, String>> context;

		
	public SourceHandler(PircBotX sourceBot, PircBotX destBot, Properties props) {
		super();
		this.sourceBot = sourceBot;
		this.destBot = destBot;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized in SourceHandler.");
		celebList = new LinkedList<String>();
		this.props = props;
		context = new HashMap<String, HashMap<String, String>>();
	}
			
	public void onMessage(MessageEvent event) {

		String message = event.getMessage();
		String destChannel = props.getProperty("dest_irc_channel");

		boolean hit = false;
		String talker = event.getUser().getNick();
		String channel = event.getChannel().getName();
		String channelText = "";
		if (this.props.getProperty("multichannel") == "true") {
			channelText = "[" + channel + "]";
		}

		for (String s : celebList) {
			if (s.equals(talker.toLowerCase())) {
				hit = true;
			}
		}
		if (hit) {
			System.out.println("ENTERING CELEB HIT LOOP");
			// check if context necessary and send thorugh celeb message
			Scanner messageScanner = new Scanner(message.toLowerCase()).useDelimiter("[:,\\?>]?\\s+");
			while (messageScanner.hasNext()) {
				String token = messageScanner.next();
				System.out.println("CHECKING ON TOKEN: "+token);
				String lowerToken = token.toLowerCase();
				HashMap<String, String> channelContext = context.get(channel);
				if (channelContext != null) {
					String contextMessage = channelContext.get(lowerToken);
					if (contextMessage != null) {
						System.out.println("CONTEXT RECEIVED - SENDING MESSAGE "+ contextMessage);
						destBot.sendMessage(destChannel, Colors.BOLD+channelText+"[CONTEXT]"+"["+token+"] "+contextMessage);
						context.remove(lowerToken);
					}
				}
			}
			destBot.sendMessage(destChannel, Colors.BOLD+channelText+"["+talker+"] "+message);
		} else {
			System.out.println("ENTERING CONTEXT PUT CLAUSE");
			// not from a celeb, store it for context
			HashMap<String, String> toAdd = context.get(channel);
			if (toAdd == null) {
				toAdd = new HashMap<String, String>();
				context.put(channel, toAdd);
			}
			toAdd.put(talker.toLowerCase(), message);
		}


	}

	public void addCeleb(String s) {
		celebList.add(s);
	}

	public void purgeCelebs() {
		celebList = new LinkedList<String>();
	}

	public LinkedList<String> listCelebs() {
		return celebList;
	}
}

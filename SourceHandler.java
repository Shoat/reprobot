import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import java.util.Properties;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;

public class SourceHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX sourceBot;
	private PircBotX destBot;
	private LinkedList<String> celebList;
	private Properties props;
		
	public SourceHandler(PircBotX sourceBot, PircBotX destBot, Properties props) {
		super();
		this.sourceBot = sourceBot;
		this.destBot = destBot;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized in SourceHandler.");
		celebList = new LinkedList<String>();
		this.props = props;
	}
			
	public void onMessage(MessageEvent event) {

		String message = event.getMessage();
		String destChannel = props.getProperty("dest_irc_channel");

		boolean hit = false;
		String talker = event.getUser().getNick();

		for (String s : celebList) {
			if (s.equals(talker)) {
				hit = true;
			}
		}
		if (hit) {
			destBot.sendMessage(destChannel, message);
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

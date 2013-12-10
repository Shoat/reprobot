import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.PircBotX;
import java.util.Scanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

public class DestHandler extends ListenerAdapter {


	private Scanner scanner;
	private PermissionsManager pm;
	private PircBotX sourceBot;
	private PircBotX destBot;
	private Properties props;
	private SourceHandler sh;
		
	public DestHandler(PircBotX sourceBot, PircBotX destBot, SourceHandler sh, Properties props) {
		super();
		this.sourceBot = sourceBot;
		this.destBot = destBot;
		this.sh = sh;
		this.props = props;
		this.pm = PermissionsManager.getInstance(); 
		System.out.println("PermissionsHandler initialized for DestHandler.");
	}
			
	public void onMessage(MessageEvent event) {

		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		scanner = new Scanner(commandLower);

		if (scanner.hasNext("!stalk")) {
			
			scanner.next();
			if (!pm.isAllowed(command,event.getUser(),event.getChannel())) {
				event.respond("Sorry, you do not have permission to stalk dudes.");
				return;
			} else {
				if (scanner.hasNext("add")) {
					//add a dude
					scanner.next();
					if (scanner.hasNext()) {
						String target = scanner.next();
						sh.addCeleb(target);
						event.respond("Added "+target+" to celeb list.");
					} else {
						event.respond("Who am I adding, exactly?  Spelling counts.");
					}
				} else if (scanner.hasNext("purge")) {
					//kill the list
					sh.purgeCelebs();
					event.respond("Purged celeb list.");
				} else {
					event.respond("I am a tiger.");
				}
			}
		}
		if (scanner.hasNext("!status")) {
			event.respond("Oh yeah, I'll get right on that.");
		}

	}
}

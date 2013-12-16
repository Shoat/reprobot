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

		if (scanner.hasNext("!echo")) {
			
			scanner.next();
			if (false) {
				event.respond("Sorry, you do not have permission to manipulate the echo list.");
				return;
			} else {
				if (scanner.hasNext("add")) {
					//add a dude
					scanner.next();
					if (scanner.hasNext()) {
						String target = scanner.next();
						sh.addCeleb(target);
						event.respond("Added "+target+" to echo list.");
					} else {
						event.respond("Who am I adding, exactly?  Spelling counts.");
					}
				} else if (scanner.hasNext("purge")) {
					//kill the list
					sh.purgeCelebs();
					event.respond("Purged echo list.");
				} else if (scanner.hasNext("list")) {
					//who is stalked?
					LinkedList<String> celebs = sh.listCelebs();
					String peeps ="";
					for (String s : celebs) {
						peeps += (s+" ");
					}
					event.respond("Tracking " + peeps);
				} else {
					event.respond("I am a tiger.");
				}
			}
		}
		if (scanner.hasNext("!bootstrap")) {
			sh.addCeleb("tiy");
			sh.addCeleb("tiyuri");
			sh.addCeleb("bartwe");
			sh.addCeleb("bartwe_");
			sh.addCeleb("mollygos");
			sh.addCeleb("kyren");
			sh.addCeleb("omnipotententity");
			
			event.respond("Sane echo defaults implemented.");
		}
		if (scanner.hasNext("!status")) {
			event.respond("Oh yeah, I'll get right on that.");
		}

	}
}

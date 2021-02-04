package de.wolle;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import de.wolle.listener.CommandListener;

public class WerwolfBot {

	public static void main(String[] args) {
		new WerwolfBot();

	}

	public WerwolfBot() {
		DiscordApi api = new DiscordApiBuilder()
				.setToken("ODA1ODE2OTU1NjEyODIzNTUy.YBgZhQ.tmCLvFFASFpE8dkXU12G4yrcH - 0").login().join();
		api.addListener(CommandListener.getInstance());
	}
}

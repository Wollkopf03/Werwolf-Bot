package com.Wollkopf03.Werwolf_Bot.lobby;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.Wollkopf03.Werwolf_Bot.listeners.CommandListener;
import com.Wollkopf03.Werwolf_Bot.player.Player;
import com.Wollkopf03.Werwolf_Bot.player.Role;

public class Lobby {

	private String code;
	private String password;
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Optional<User>> bannedPlayers = new ArrayList<Optional<User>>();
	private HashMap<Role, Integer> roles = new HashMap<Role, Integer>();
	private Optional<User> host;
	private Server server;
	private boolean running;

	public Lobby(Server server, String code, String password, Optional<User> host) {
		this.server = server;
		this.code = code;
		this.password = password;
		this.host = host;
		players.add(new Player(host));
		roles.put(Role.amor, 1);
		roles.put(Role.bürger, 1);
		roles.put(Role.hexe, 1);
		roles.put(Role.jäger, 1);
		roles.put(Role.mädchen, 1);
		roles.put(Role.seher, 1);
		roles.put(Role.werwolf, 3);
		running = false;
	}

	public String getCode() {
		return code;
	}

	public Optional<User> getHost() {
		return host;
	}

	public boolean tryJoin(Optional<User> user, String password) {
		if (bannedPlayers.contains(user))
			return false;
		for (Player player : players)
			if (player.getUser().equals(user))
				break;
			else if (this.password.equals(password)) {
				players.add(new Player(user));
				return true;
			}
		return false;
	}

	public boolean tryLeave(Optional<User> user, EmbedBuilder embed) {
		for (Player player : players) {
			if (player.getUser().equals(user))
				players.remove(player);
			else
				continue;
			if (host.equals(user))
				if (players.size() != 0) {
					host = players.get(0).getUser();
					host.get()
							.sendMessage(new EmbedBuilder().setTitle("You are the new host!")
									.setDescription("The Host leaved the Lobby, so you are the new Host :clap:")
									.addField("About starting the Lobby", "To start the Lobby type \"!start\""));
				} else
					close(embed);
			return true;
		}
		return false;

	}

	public boolean tryStart(Optional<User> user) {
		if (host.equals(user)) {
			start();
			return true;
		}
		return false;
	}

	public boolean tryClose(Optional<User> user, EmbedBuilder embed) {
		if (host.equals(user)) {
			close(embed);
			return true;
		}
		return false;
	}

	public boolean hasPlayer(Optional<User> user) {
		for (Player player : players)
			if (player.getUser().equals(user))
				return true;
		return false;
	}

	private void close(EmbedBuilder embed) {
		CommandListener.getInstance().closeLobby(this, embed);
	}

	public void getRoles(EmbedBuilder embed) {
		System.out.println("Roles");
		embed.setTitle("Roles").addField("Here the roles: ", listRoles()).setColor(Color.BLUE);
	}

	public void getStats(EmbedBuilder embed) {
		embed.setTitle("Stats").setDescription("Here the current Ranking: ").addField("Top 10", getRanking())
				.setColor(Color.BLUE);
	}

	private String listRoles() {
		System.out.println("Roles");
		String string = "";
		for (Role key : roles.keySet())
			string += "\n" + key + ": " + roles.get(key);
		System.out.println(string);
		return string;
	}

	private String getRanking() {
		String string = "";
		int highestScore = 0, rank = 1;
		for (Player player : players)
			if (player.getScore() > highestScore)
				highestScore = player.getScore();
		for (int i = highestScore; i >= 0; i--) {
			for (Player player : players)
				if (player.getScore() == i)
					string += "\n" + rank + ". " + player.getUser().get().getDisplayName(server) + ": "
							+ player.getScore();
			rank++;
			if (rank == 11)
				break;
		}
		for (; rank < 11; rank++) {
			string += "\n" + rank + ". ";
		}
		return string;
	}

	public void addRole(Role role) {
		roles.replace(role, roles.get(role) + 1);
	}

	public boolean removeRole(Role role) {
		System.out.println(roles.get(role));
		if (role.equals(Role.werwolf)) {
			if (roles.get(role) > 1) {
				roles.replace(role, roles.get(role) - 1);
				return true;
			}
		} else if (roles.get(role) > 0) {
			roles.replace(role, roles.get(role) - 1);
			return true;
		}
		return false;
	}

	public boolean running() {
		return running;
	}

	public boolean kickPlayer(String username) {
		ArrayList<Player> temp = new ArrayList<Player>();
		temp.addAll(players);
		for (Player player : temp) {
			if (player.getUser().get().getDisplayName(server).equals(username)) {
				players.remove(player);
				return true;
			}
		}
		return false;
	}

	public boolean banPlayer(String username) {
		ArrayList<Player> temp = new ArrayList<Player>();
		temp.addAll(players);
		for (Player player : temp) {
			if (player.getUser().get().getDisplayName(server).equals(username)) {
				bannedPlayers.add(player.getUser());
				players.remove(player);
				return true;
			}
		}
		return false;
	}

	public void showConfig(EmbedBuilder embed) {
		String playersString = "", bannedPlayersString = "", rolesString = "";
		for (Player player : players)
			playersString += player.getUser().get().getDisplayName(server) + "\n";
		for (Optional<User> player : bannedPlayers)
			bannedPlayersString += player.get().getDisplayName(server) + "\n";
		rolesString += listRoles();
		embed.setTitle("Lobby Config").setDescription("The current Configuration, players, and roles of this Lobby.")
				.addField("Server", server.getName()).addField("Host", host.get().getDisplayName(server))
				.addField("Players", playersString).addField("Roles", rolesString).setColor(Color.BLUE);
		if (bannedPlayers.size() != 0) {
			embed.addField("Banned Players", bannedPlayersString);
		}
	}

	private void start() {
		int rolescount = 0;
		for (Role role : roles.keySet())
			rolescount += roles.get(role);
		if (players.size() != rolescount)
			return;
		running = true;

	}
}
package com.Wollkopf03.Werwolf_Bot.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.Wollkopf03.Werwolf_Bot.lobby.Lobby;
import com.Wollkopf03.Werwolf_Bot.player.Role;

public class CommandListener implements MessageCreateListener {

	private static CommandListener instance;

	private ArrayList<String> commands = new ArrayList<String>();
	private ArrayList<Lobby> lobbys = new ArrayList<Lobby>();

	public CommandListener() {
		commands.add("commands");
		commands.add("open");
		commands.add("start");
		commands.add("close");
		commands.add("join");
		commands.add("leave");
		commands.add("stats");
		commands.add("roles");
		commands.add("add");
		commands.add("remove");
		commands.add("kick");
		commands.add("ban");
		commands.add("configs");
	}

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		Message message = event.getMessage();
		String text = event.getMessageContent();

		Optional<User> user = message.getUserAuthor();
		TextChannel channel = null;
		try {
			channel = user.get().openPrivateChannel().get();
		} catch (InterruptedException | ExecutionException e) {
		}
		if (text.startsWith("!")) {
			String[] args = text.substring(1).split(" ");
			if (args.length != 0) {
				EmbedBuilder embed = new EmbedBuilder();
				switch (args[0].toLowerCase()) {
				case "commands":
					if (args.length == 1) {
						embed.setTitle("Commands").setDescription("Here is our CommandList: ")
								.addField("Commands", getCommands()).setColor(Color.BLUE);
					}
					break;
				case "open":
					if (args.length == 2) {
						String code = generateLobbyCode();
						lobbys.add(new Lobby(message.getServer().get(), code, args[1], user));
						embed.setTitle("New Lobby opened").setDescription("The code of your Lobby is " + code)
								.addField("You can join the Lobby now",
										"type \"!join " + code + " <password>\" to join the Lobby")
								.setColor(Color.BLUE);
						channel.sendMessage(embed);
						embed = new EmbedBuilder();
						embed.setTitle("You joined successfully: " + user.get().getDisplayName(event.getServer().get()))
								.setDescription("you are the host, you can start the game with\"!start\"")
								.setColor(Color.BLUE);
					} else {
						embed.setTitle("Lobby creation failed")
								.addField("To open a Lobby please type:", "\"!open <password>\" to open the Lobby")
								.setFooter("Note:\nYour password cannot contain spaces!").setColor(Color.RED);
					}
					break;
				case "start":
					if (args.length == 1) {
						for (Lobby lobby : lobbys)
							if (lobby.tryStart(user))
								embed.setTitle("Lobby started")
										.setDescription("The Lobby " + lobby.getCode() + " started successfully")
										.addField("Joining closed", "You cannot join the Lobby anymore")
										.setColor(Color.BLUE);
							else
								embed.setTitle("Lobby starting failed").setDescription(
										"You are not the host of the Lobby, please wait until the host starts the Lobby.")
										.setColor(Color.RED);
					} else {
						embed.setTitle("Lobby starting failed").addField("To start a Lobby please type:", "\"!start\"")
								.setColor(Color.RED);
					}
					break;
				case "close":
					if (args.length == 1) {
						boolean closed = false;
						ArrayList<Lobby> temp = new ArrayList<Lobby>();
						temp.addAll(lobbys);
						for (Lobby lobby : temp)
							if (lobby.tryClose(user, embed))
								closed = true;
						if (!closed)
							embed.setTitle("Failed closing Lobby").setDescription("You are not the host of the Lobby.")
									.setColor(Color.RED);
					} else {
						embed.setTitle("Failed closing Lobby").addField("To close a Lobby please type:", "\"!close\" ")
								.setColor(Color.RED);
					}
					break;
				case "join":
					if (args.length == 3) {
						if (getLobby(args[1]) != null) {
							if (getLobby(args[1]).tryJoin(user, args[2])) {
								embed.setTitle("You joined successfully: "
										+ user.get().getDisplayName(event.getServer().get()))
										.setDescription(
												"Please wait until the Host starts the Game or leave with\"!leave\"")
										.setColor(Color.BLUE);
							} else {
								embed.setTitle("Failed joining").setDescription("Wrong password or already joined")
										.setColor(Color.RED);
							}
						} else {
							embed.setTitle("Failed joining")
									.setDescription("There is no Lobby with the code " + args[1]).setColor(Color.RED);
						}

					} else {
						embed.setTitle("Failed joining")
								.setDescription("To join a Lobby type \"!join <code> <password>\"")
								.setFooter("Note:\nThe password and code cannot contain spaces!").setColor(Color.RED);
					}
					break;
				case "leave":
					if (args.length == 1) {
						boolean leaved = false;
						ArrayList<Lobby> temp = new ArrayList<Lobby>();
						temp.addAll(lobbys);
						for (Lobby lobby : temp) {
							if (lobby.tryLeave(user, embed)) {
								leaved = true;
								embed.setTitle("You leaved successfully: "
										+ user.get().getDisplayName(event.getServer().get())).setColor(Color.BLUE);
							}
						}
						if (!leaved) {
							embed.setTitle("Failed leaving: " + user.get().getDisplayName(event.getServer().get()))
									.setDescription("You are currently not in a lobby.").setColor(Color.RED);
						}
					} else {
						embed.setTitle("Failed leaving").setDescription("To leave a Lobby type \"!leave\"")
								.setColor(Color.RED);
					}
					break;
				case "stats":
					if (args.length == 1) {
						boolean showed = false;
						for (Lobby lobby : lobbys) {
							if (lobby.hasPlayer(user)) {
								showed = true;
								lobby.getStats(embed);
							}
						}
						if (!showed) {
							embed.setTitle("Failed showing stats").setDescription("You are currently not in a lobby.")
									.setColor(Color.RED);
						}
					} else {
						embed.setTitle("Faied showing stats").setDescription("To show stats type \"!stats\"")
								.setColor(Color.RED);
					}
					break;
				case "roles":
					if (args.length == 1) {
						boolean showed = false;
						for (Lobby lobby : lobbys) {
							if (lobby.hasPlayer(user)) {
								System.out.println("Roles");
								showed = true;
								lobby.getRoles(embed);
							}
						}
						if (!showed) {
							embed.setTitle("Failed showing roles").setDescription("You are currently not in a lobby.")
									.setColor(Color.RED);
						}
					} else {
						embed.setTitle("Faied showing roles").setDescription("To show roles type \"!roles\"")
								.setColor(Color.RED);
					}
					break;
				case "configs":
					if (args.length == 1) {
						boolean showed = false;
						for (Lobby lobby : lobbys) {
							if (lobby.hasPlayer(user)) {
								showed = true;
								lobby.showConfig(embed);
							}
						}
						if (!showed)
							embed.setTitle("Failed showing Lobbyconfiguration")
									.setDescription("You are currently not in a Lobby").setColor(Color.RED);
					} else
						embed.setTitle("Failed showing Lobbyconfiguration")
								.setDescription("To showing Lobbyconfiguration type \"!showLobby\"")
								.setColor(Color.RED);
					break;
				case "add":
					boolean showed = false;
					for (Lobby lobby : lobbys)
						if (lobby.getHost().equals(user))
							if (!lobby.running())
								if (args.length != 1) {
									lobby.addRole(parseRole(text.substring(5)));
									showed = true;
									embed.setTitle("Role " + text.substring(5) + " successfully added")
											.setColor(Color.BLUE);
								}
					if (!showed)
						embed.setTitle("Failed adding Role")
								.setDescription("The game is currently running. You can stop the game with \"!stop\"")
								.setColor(Color.RED);
					break;
				case "remove":
					for (Lobby lobby : lobbys)
						if (lobby.getHost().equals(user))
							if (!lobby.running()) {
								if (args.length != 1)
									if (lobby.removeRole(parseRole(text.substring(8))))
										embed.setTitle("Role " + text.substring(8) + " successfully removed")
												.setColor(Color.BLUE);
									else if (text.substring(8).toLowerCase().equals("werwolf"))
										embed.setTitle("Failed removing role " + text.substring(8))
												.setDescription("You cant have less than 1 werewolves")
												.setColor(Color.RED);
									else
										embed.setTitle("Failed removing role " + text.substring(8))
												.setDescription("The role " + text.substring(8) + " is currently on 0")
												.setColor(Color.RED);
								else
									embed.setTitle("Failed removing role")
											.setDescription("Type \"!remove <role>\" to remove role")
											.setColor(Color.RED);
							} else
								embed.setTitle("Failed removing role")
										.setDescription(
												"The game is currently running. You can stop the game with \"!stop\"")
										.setColor(Color.RED);
					break;
				case "kick":
					for (Lobby lobby : lobbys)
						if (lobby.getHost().equals(user))
							if (args.length != 1) {
								if (lobby.kickPlayer(text.substring(6)))
									embed.setTitle("Player " + text.substring(6) + "successfully kicked")
											.setColor(Color.BLUE);
								else
									embed.setTitle("Failed kicking player " + text.substring(6)).setDescription(
											"There is no player with the name " + text.substring(6) + " in your Lobby")
											.setColor(Color.RED);
							} else
								embed.setTitle("Failed kicking player")
										.setDescription("To kick a player type \"!kick <name>\"").setColor(Color.RED);
						else
							embed.setTitle("Failed kicking player").setDescription("You are not the host of a Lobby")
									.setColor(Color.RED);
					break;
				case "ban":
					for (Lobby lobby : lobbys)
						if (lobby.getHost().equals(user))
							if (args.length != 1) {
								if (lobby.banPlayer(text.substring(5)))
									embed.setTitle("Player " + text.substring(6) + "successfully banned")
											.setColor(Color.BLUE);
								else
									embed.setTitle("Failed banning player " + text.substring(6)).setDescription(
											"There is no player with the name " + text.substring(6) + " in your Lobby")
											.setColor(Color.RED);
							} else
								embed.setTitle("Failed banning player")
										.setDescription("To ban a player type \"!ban <name>\"").setColor(Color.RED);
						else
							embed.setTitle("Failed banning player").setDescription("You are not the host of a Lobby")
									.setColor(Color.RED);
					break;
				default:
					embed.setTitle("Invalid Command").setDescription("Please use a valid command!")
							.addField("Commands:", getCommands()).setColor(Color.RED);
				}
				embed.setAuthor("Werwolf");
				message.delete();
				channel.sendMessage(embed);
			}
		}

	}

	private Role parseRole(String substring) {
		return Role.werwolf;
	}

	private Lobby getLobby(String code) {
		for (Lobby lobby : lobbys) {
			if (lobby.getCode().equals(code))
				return lobby;
		}
		return null;
	}

	private String generateLobbyCode() {
		String code;
		Random ran = new Random();
		boolean unique = true;
		do {
			code = "";
			unique = true;
			for (int i = 0; i < 8; i++)
				code += ran.nextInt(10);
			for (Lobby lobby : lobbys) {
				if (lobby.getCode().equals(code))
					unique = false;
			}
		} while (!unique);
		return code;
	}

	private String getCommands() {
		String string = "";
		for (String com : commands)
			string += "\n" + com;
		return string;
	}

	public void closeLobby(Lobby lobby, EmbedBuilder embed) {
		if (lobbys.contains(lobby))
			lobbys.remove(lobby);
		embed.setTitle("Lobby closed").setDescription("The Lobby " + lobby.getCode() + " successfully closed.")
				.setColor(Color.BLUE);
	}

	public static CommandListener getInstance() {
		if (instance == null)
			instance = new CommandListener();
		return instance;
	}

	public ArrayList<Lobby> getLobbys() {
		return lobbys;
	}
}

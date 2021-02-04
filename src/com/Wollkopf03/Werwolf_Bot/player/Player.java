package com.Wollkopf03.Werwolf_Bot.player;

import java.util.Optional;

import org.javacord.api.entity.user.User;

public class Player {

	private Optional<User> user;
	private int score;
	private Role role;

	public Player(Optional<User> user) {
		this.user = user;
	}

	public void start(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public Optional<User> getUser() {
		return user;
	}

	public int getScore() {
		return score;
	}

	public void raiseScore(int points) {
		score += points;
	}
}

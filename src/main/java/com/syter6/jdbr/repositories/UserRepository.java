package com.syter6.jdbr.repositories;

import com.syter6.jdbr.BaseRepository;
import com.syter6.jdbr.models.User;

public class UserRepository extends BaseRepository<User> {

	public UserRepository() {
		super("users", User::new);
	}
}

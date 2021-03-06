/**
 * Diego Moreno Lennon
 * Burger Javi's Server
 */

package com.burgerjavis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.burgerjavis.entities.User;

public class Credentials {
	
	private String username;
	private String password;
	private List<String> roles;
	
	public Credentials(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.roles = new ArrayList<String>();
		for(GrantedAuthority role : user.getRoles()) {
			this.roles.add(role.getAuthority());
		}
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}

	public List<String> getRoles() {
		return roles;
	}
	
	
}

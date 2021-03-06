/**
 * Diego Moreno Lennon
 * Burger Javi's Server
 */

package com.burgerjavis.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.burgerjavis.Common;
import com.burgerjavis.ErrorText;
import com.burgerjavis.ErrorText.ErrorCause;
import com.burgerjavis.entities.User;
import com.burgerjavis.mvc.wrappers.UserWrapper;
import com.burgerjavis.repositories.UserRepository;
import com.burgerjavis.validation.UserValidator;

@Controller
@RequestMapping("/webclient/user**")
public class BurgerJavisMVCUser {
	
	@Autowired
	UserRepository userRepository;
	
	@RequestMapping (value = "/add", method = RequestMethod.GET)
	public ModelAndView addCategory() {
		UserWrapper userWrapper = new UserWrapper();
		return new ModelAndView("add_user").addObject("user", userWrapper);
	}
	
	@RequestMapping (value = "/add", method = RequestMethod.POST)
	public ModelAndView addCategory(UserWrapper user) {
		final String errorText = "ERROR CREANDO USUARIO";
		User newUser = user.getInternalType();
		if(!UserValidator.validateUser(newUser)) {
			ErrorCause cause = ErrorCause.INVALID_DATA;
			return new ModelAndView("add_user").addObject("user", user).
					addObject("error", new ErrorText(errorText, cause));
		}
		if(userRepository.findByUsernameIgnoreCase(newUser.getUsername()) != null) {
			ErrorCause cause = ErrorCause.NAME_IN_USE;
			return new ModelAndView("add_user").addObject("user", user).
					addObject("error", new ErrorText(errorText, cause));
		}
		newUser.setPassword(new BCryptPasswordEncoder().encode(newUser.getPassword()));
		userRepository.save(newUser);
		return new ModelAndView("redirect:/");
	}
	
	@RequestMapping (value = "/get{id}", method = RequestMethod.GET)
	public ModelAndView getUser(String id) {
		User user = userRepository.findOne(id);
		UserWrapper userWrapper = new UserWrapper();
		userWrapper.wrapInternalType(user);
		return new ModelAndView("edit_user").addObject("user", userWrapper);
	}
	
	@RequestMapping (value= "/modify{id}", method = RequestMethod.PUT)
	public ModelAndView modifyUser (String id, UserWrapper user) {
		User modUser = user.getInternalType();
		final String errorText = "ERROR ACTUALIZANDO USUARIO";
		User currentUser = userRepository.findOne(id);
		if(currentUser == null) {
			return new ModelAndView("edit_user").addObject("user", user).
					addObject("error", new ErrorText(errorText, ErrorCause.NOT_FOUND));
		}
		if(!UserValidator.validateUser(modUser)) {
			return generateError(currentUser, errorText, ErrorCause.INVALID_DATA);
		}
		// Role modified, check minimum number of admins
		ModelAndView resultError = checkNumberOfAdmins(currentUser, user, errorText);
		if (resultError != null) {
			return resultError;
		}
		// Check if name is modified
		ModelAndView resultName = checkNameModified(currentUser, modUser, errorText);
		if (resultName != null) {
			return resultName;
		}
		modUser.setPassword(new BCryptPasswordEncoder().encode(modUser.getPassword()));
		currentUser.updateUser(modUser);
		userRepository.save(currentUser);
		return new ModelAndView("redirect:/");
	}
	
	@RequestMapping (value= "/delete{id}", method = RequestMethod.DELETE)
	public ModelAndView deleteUser (String id) {
		final String errorText = "ERROR BORRANDO USUARIO";
		User currentUser = userRepository.findOne(id);
		if(currentUser == null) {
			return generateError(currentUser, errorText, ErrorCause.NOT_FOUND);
		}
		if(currentUser.isAdmin()) {
			ModelAndView resultError = returnErrorWhenOnlyOneAdminLeft(currentUser, errorText);
			if (resultError != null) {
				return resultError;
			}
		}
		userRepository.delete(currentUser);
		return new ModelAndView("redirect:/");
	}
	
	private ModelAndView returnErrorWhenOnlyOneAdminLeft(User currentUser, String errorText) {
		int admins = 0;
		for(User lUser : userRepository.findAll()) {
			if(lUser.isAdmin()) {
				admins++;
			}
		}
		if (admins <= Common.MIN_ADMINS) {
			return generateError(currentUser, errorText, ErrorCause.MIN_ADMINS);
		}
		return null;
	}
	
	private ModelAndView checkNameModified (User currentUser, User modUser, String errorText) {
		if(!modUser.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
			// Name has been modified
			if(userRepository.findByUsernameIgnoreCase(modUser.getUsername()) != null) {
				return generateError(currentUser, errorText, ErrorCause.NAME_IN_USE);
			}
		}
		return null;
	}
	
	private ModelAndView generateError (User currentUser, String errorText, ErrorCause cause) {
		UserWrapper userWrapper = new UserWrapper();
		userWrapper.wrapInternalType(currentUser);
		return new ModelAndView("edit_user").addObject("user", userWrapper).
				addObject("error", new ErrorText(errorText, cause));
	}
	
	private ModelAndView checkNumberOfAdmins (User currentUser, UserWrapper user, String errorText) {
		if(currentUser.isAdmin() && !user.getRole().equalsIgnoreCase(Common.ADMIN_ROLE)) {
			ModelAndView resultError = returnErrorWhenOnlyOneAdminLeft(currentUser, errorText);
			if (resultError != null) {
				return resultError;
			}
		}
		return null;
	}
	
}

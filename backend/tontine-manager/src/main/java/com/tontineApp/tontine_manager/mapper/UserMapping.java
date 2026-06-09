package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.UserRequest;
import com.tontineApp.tontine_manager.dto.UserResponse;
import com.tontineApp.tontine_manager.model.Role;
import com.tontineApp.tontine_manager.model.Users;
import org.springframework.stereotype.Component;


@Component
public class UserMapping {

    public UserResponse mapToUserResponse(Users user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setNom(user.getNom());
        userResponse.setPrenom(user.getPrenom());
        userResponse.setEmail(user.getEmail());
        userResponse.setTelephone(user.getTelephone());
        userResponse.setDateInscription(user.getDateInscription());
        userResponse.setVille(user.getVille());
        userResponse.setId(user.getId());

        userResponse.setRoles(
                user.getRoles().stream()
                        .map(Role::getNomRole)
                        .toList()
        );

        return userResponse;
    }
    public Users mapUserRequestToUser(UserRequest userRequest) {
        Users user = new Users();
        user.setNom(userRequest.getNom());
        user.setPrenom(userRequest.getPrenom());
        user.setEmail(userRequest.getEmail());
        user.setDateInscription(userRequest.getDateInscription());
        user.setUserPassword(userRequest.getPassword());
        user.setVille(userRequest.getVille());
        user.setTelephone(userRequest.getTelephone());
        return user;
    }
}

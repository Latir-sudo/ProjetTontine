package com.tontineApp.tontine_manager.service;


import com.tontineApp.tontine_manager.dto.UserRequest;
import com.tontineApp.tontine_manager.dto.UserResponse;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.UserMapping;
import com.tontineApp.tontine_manager.model.Role;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.RoleRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tontineApp.tontine_manager.enumeration.StatutAdhesion.ATTENTE;

@Service
@AllArgsConstructor
public class UserService {

    // récupération utilisateurs
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapping userMapping;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse getById(Integer id) {
        return userMapping.mapToUserResponse(userRepository.findById(id).orElseThrow(()-> new RessourceNotFoundException("User not found")));

    }
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream()
                .map(userMapping::mapToUserResponse)
                .toList();
    }

    public UserResponse getUserByTelephone(String telephone){
        return userMapping.mapToUserResponse(userRepository.findByTelephone(telephone).orElseThrow(()-> new RessourceNotFoundException("ce numéro ne correspond à aucun user")));
    }

    @Transactional
    public UserResponse saveUser(UserRequest userRequest) {

        if(userRepository.findByTelephone(userRequest.getTelephone()).isPresent()){
            throw new IllegalArgumentException("le numero existe deja");
        }

        if(userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            throw new IllegalArgumentException("l'email existe deja");
        }

        Users user = userMapping.mapUserRequestToUser(userRequest);

        for (String rolename :userRequest.getRoles()) {

            Role role = roleRepository.findByNomRole(rolename).
                    orElseThrow(()-> new RessourceNotFoundException("Role not found"));

            user.addRole(role);
            user.setStatutCompte("attente");
        }
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        return userMapping.mapToUserResponse(userRepository.save(user));
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse updateUser(UserRequest newuser,Integer id){

        if(newuser==null){
            throw new IllegalArgumentException("objet vide");

        }
        Users user= userRepository.findById(id).orElseThrow(()-> new RessourceNotFoundException("User not found"));
        if(newuser.getNom()!=null && !newuser.getNom().isBlank())
            user.setNom(newuser.getNom());
        if(newuser.getPrenom()!=null && !newuser.getPrenom().isBlank())
            user.setPrenom(newuser.getPrenom());
        if(newuser.getPassword()!=null && !newuser.getPassword().isBlank())
            user.setUserPassword(newuser.getPassword());
        if(newuser.getEmail()!=null && !newuser.getEmail().isBlank())
            user.setEmail(newuser.getEmail());
        if(newuser.getTelephone()!=null  && !newuser.getTelephone().isBlank())
            user.setTelephone(newuser.getTelephone());

        return userMapping.mapToUserResponse(userRepository.save(user));

    }

    public boolean existsByEmail(String email){
        return  userRepository.existsByEmail(email);
    }

    public UserResponse getUserByEmail(String email){
        return userMapping.mapToUserResponse(userRepository.findByEmail(email).orElseThrow(()-> new RessourceNotFoundException("User not found")));
    }


}

package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="utilisateurs")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private Integer id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    @Column(name="userpassword")
    private String userPassword;
    @Column(name="date_inscription")
    private LocalDate dateInscription;
    @Column(name="statut_compte")
    private String statutCompte;
    private String ville;

    @ManyToMany
    @JoinTable(name = "users_roles" ,joinColumns = @JoinColumn(name="user_id"),inverseJoinColumns = @JoinColumn(name="role_id"))
    private List<Role> roles=new ArrayList<>();



    public void addRole(Role role){
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role){
        roles.remove(role);
        role.getUsers().remove(this);
    }

}

package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table (name="role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_role")
    private Integer id;

    @Column(name="nom_role")
    private String nomRole;

    @Column(name="description_role")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private List<Users> users=new ArrayList<>();

}

package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="invitation")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_invitation")
    private Integer id;

    @Column(name="code_invitation")
    private String CodeInvitation;
    @Column(name="description_invitation")
    private String DescriptionInvitation;
    @Column(name="date_invitation")
    private LocalDate dateInvitation;
    @Column(name="statut_invitation")
    private String StatutInvitation;

    @ManyToOne
    @JoinColumn(name="tontine_id")
    private Tontine tontine;

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name="user_id"),inverseJoinColumns = @JoinColumn(name="invitation_id"))
    private List<Users> users=new ArrayList<>();
}

package com.gitanalytics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {
    @Id
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "team_id", length = 64)
    private String teamId;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "role", length = 64)
    private String role;
}

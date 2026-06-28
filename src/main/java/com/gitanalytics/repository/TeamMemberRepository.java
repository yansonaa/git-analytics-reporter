package com.gitanalytics.repository;

import com.gitanalytics.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {

    List<TeamMember> findByTeamId(String teamId);

    List<TeamMember> findByTeamIdAndActiveTrue(String teamId);
}

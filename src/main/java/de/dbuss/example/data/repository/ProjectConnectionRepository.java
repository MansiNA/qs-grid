package de.dbuss.example.data.repository;

import de.dbuss.example.data.entity.ProjectConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectConnectionRepository extends JpaRepository<ProjectConnection, Long> {
    Optional<ProjectConnection> findByName(String name);
}

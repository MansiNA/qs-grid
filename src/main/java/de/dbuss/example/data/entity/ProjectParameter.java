package de.dbuss.example.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_parameter", schema = "dbo")
public class ProjectParameter {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated ID
    @Column(name = "id")
    private Integer id;

    @Column(name = "Namespace")
    private String namespace;

    @Column(name = "Name")
    private String name;

    @Column(name = "Value")
    private String value;

    @Column(name = "Description")
    private String description;

}

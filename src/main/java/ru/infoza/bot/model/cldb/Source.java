package ru.infoza.bot.model.cldb;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "sources")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Size(max = 255)
    @NotNull
    @Column(name = "url")
    private String url;

    @Column(name = "created")
    private OffsetDateTime created;

    @Column(name = "updated")
    private OffsetDateTime updated;

    @Column(name = "deleted")
    private OffsetDateTime deleted;

}
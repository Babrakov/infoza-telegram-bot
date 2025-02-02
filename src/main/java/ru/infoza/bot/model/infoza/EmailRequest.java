package ru.infoza.bot.model.infoza;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lnze")
public class EmailRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int not null")
    private Long id;

    @Column(name = "id_email", columnDefinition = "int UNSIGNED not null")
    private Long idEmail;

    @Column(name = "in_ist", columnDefinition = "int UNSIGNED not null")
    private Long inIst;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ip", length = 15)
    private String ip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_email", referencedColumnName = "id", insertable = false, updatable = false)
    private Email email;

}

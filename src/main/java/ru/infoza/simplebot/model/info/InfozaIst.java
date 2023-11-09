package ru.infoza.simplebot.model.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "z_ist")
public class InfozaIst {

    @Id
    private Long idIST;
    private String vcUSR;
    private String vcFIO;
    private String vcORG;
    private Timestamp dtWRK;
    private Timestamp dtBBS;
    private Timestamp dtCRE;
    private String vcSOT;
    private String vcWRK;
    private String vcVNU;
    private String vcMAIL;
    private Timestamp dtGB0;
    private Timestamp dtGB1;
    private Timestamp dtGB2;
    private Timestamp dtGB3;
    private Timestamp dtGB4;
    private Timestamp dtMO;
    private Timestamp dtMT;
    private Timestamp dtBED;
    private String vcSTATUS;
    private String vcBIRTH;
    private String vcDOLGN;
    private Timestamp dtCBR;

}

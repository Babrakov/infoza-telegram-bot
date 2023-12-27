package ru.infoza.bot.model.bot;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@ToString
@Entity(name = "bot_users")
public class BotUser {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    private int tip;
    private int grp;
    private int ist;

    @Column(name = "remain_phone_reqs")
    private int remainPhoneReqs;

    @Column(name = "remain_email_reqs")
    private int remainEmailReqs;

    @Column(name = "registered_at")
    private Timestamp registeredAt;
}

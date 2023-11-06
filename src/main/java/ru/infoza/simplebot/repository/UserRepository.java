package ru.infoza.simplebot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.infoza.simplebot.model.User;

public interface UserRepository extends CrudRepository<User,Long> {
}

package ru.infoza.bot.repository.cldb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.infoza.bot.model.cldb.Source;

@Repository
public interface SourcesRepository extends JpaRepository<Source, Integer> {
}
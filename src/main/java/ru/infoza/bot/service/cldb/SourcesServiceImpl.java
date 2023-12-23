package ru.infoza.bot.service.cldb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.infoza.bot.model.cldb.Source;
import ru.infoza.bot.repository.cldb.SourcesRepository;

@Service
public class SourcesServiceImpl implements SourcesService {

    private final SourcesRepository sourcesRepository;

    @Autowired
    public SourcesServiceImpl(SourcesRepository sourcesRepository) {
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    public Source findSourceById(int id) {
        return sourcesRepository.findById(id).orElse(null);
    }
}

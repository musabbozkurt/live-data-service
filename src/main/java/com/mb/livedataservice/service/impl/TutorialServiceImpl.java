package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.Tutorial;
import com.mb.livedataservice.data.repository.TutorialRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.service.TutorialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;

    @Override
    public List<Tutorial> findByTitleContaining(String title) {
        if (StringUtils.isNotBlank(title)) {
            return tutorialRepository.findByTitleContaining(title);
        }
        return tutorialRepository.findAll();
    }

    @Override
    public Tutorial findById(long id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    public Tutorial save(Tutorial tutorial) {
        return tutorialRepository.save(tutorial);
    }

    @Override
    public Tutorial update(long id, Tutorial tutorial) {
        Tutorial byId = findById(id);
        byId.setTitle(tutorial.getTitle());
        byId.setDescription(tutorial.getDescription());
        byId.setPublished(tutorial.isPublished());
        return tutorialRepository.save(byId);
    }

    @Override
    public void deleteById(long id) {
        tutorialRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        tutorialRepository.deleteAll();
    }

    @Override
    public List<Tutorial> findByPublished(boolean b) {
        return tutorialRepository.findByPublished(b);
    }
}
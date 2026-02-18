package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.core.interaction.api.client.EventClient;
import ru.practicum.core.interaction.api.dto.event.EventFullDto;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с подборками событий.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventRepository;
    private Map<Long, EventFullDto> eventDtoMap = new HashMap<>();

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title: {}", newCompilationDto.getTitle());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictResource("Compilation with title '" + newCompilationDto.getTitle() + "' already exists");
        }

        Compilation compilation = CompilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            compilation.setEvents(newCompilationDto.getEvents().stream().collect(Collectors.toSet()));
        } else {
            compilation.setEvents(new HashSet<>());
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            log.info("Compilation created successfully with id: {}", savedCompilation.getId());

            eventDtoMap = eventRepository.findAllByIdIn(newCompilationDto.getEvents()).stream()
                    .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

            return CompilationMapper.toDto(savedCompilation, eventDtoMap);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation creation failed due to data integrity violation");
        }
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Compilation with id: {} deleted successfully", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            if (!compilation.getTitle().equals(updateRequest.getTitle()) &&
                    compilationRepository.existsByTitle(updateRequest.getTitle())) {
                throw new ConflictResource("Compilation with title '" + updateRequest.getTitle() + "' already exists");
            }
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            compilation.setEvents(updateRequest.getEvents().stream().collect(Collectors.toSet()));
        }

        try {
            Compilation updatedCompilation = compilationRepository.save(compilation);
            log.info("Compilation with id: {} updated successfully", compId);

            eventDtoMap = eventRepository.findAllByIdIn(updateRequest.getEvents()).stream()
                    .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

            return CompilationMapper.toDto(updatedCompilation, eventDtoMap);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation update failed due to data integrity violation");
        }
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        log.info("Getting compilations with pinned={}, pageable={}", pinned, pageable);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        List<Long> eventIds = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .toList();

        eventDtoMap = eventRepository.findAllByIdIn(eventIds).stream()
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        return compilations.stream()
                .map(compilation -> CompilationMapper.toDto(compilation, eventDtoMap))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        eventDtoMap = eventRepository.findAllByIdIn(compilation.getEvents().stream().toList()).stream()
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        return CompilationMapper.toDto(compilation, eventDtoMap);
    }
}
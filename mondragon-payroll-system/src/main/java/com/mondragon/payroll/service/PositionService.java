package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.PositionDto;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.Position;
import com.mondragon.payroll.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public List<PositionDto> findAll() {
        return positionRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PositionDto> findActive() {
        return positionRepository.findByActiveTrueOrderByTitleAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PositionDto findById(Long id) {
        return toDto(getEntity(id));
    }

    @Transactional
    public PositionDto create(PositionDto dto) {
        if (positionRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new BusinessException("Position title already exists");
        }
        Position position = Position.builder()
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .active(dto.getActive() == null || dto.getActive())
                .build();
        return toDto(positionRepository.save(position));
    }

    @Transactional
    public PositionDto update(Long id, PositionDto dto) {
        Position position = getEntity(id);
        position.setTitle(dto.getTitle().trim());
        position.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            position.setActive(dto.getActive());
        }
        return toDto(positionRepository.save(position));
    }

    @Transactional
    public void delete(Long id) {
        setActive(id, false);
    }

    @Transactional
    public PositionDto activate(Long id) {
        return setActive(id, true);
    }

    @Transactional
    public PositionDto deactivate(Long id) {
        return setActive(id, false);
    }

    private PositionDto setActive(Long id, boolean active) {
        Position position = getEntity(id);
        position.setActive(active);
        return toDto(positionRepository.save(position));
    }

    public Position getEntity(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found: " + id));
    }

    private PositionDto toDto(Position p) {
        PositionDto dto = new PositionDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setActive(p.getActive());
        return dto;
    }
}

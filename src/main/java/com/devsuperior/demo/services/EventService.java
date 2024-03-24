package com.devsuperior.demo.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.demo.dto.EventDTO;
import com.devsuperior.demo.entities.City;
import com.devsuperior.demo.entities.Event;
import com.devsuperior.demo.exceptions.DatabaseException;
import com.devsuperior.demo.exceptions.ResourceNotFoundException;
import com.devsuperior.demo.repositories.CityRepository;
import com.devsuperior.demo.repositories.EventRepository;

import jakarta.persistence.EntityNotFoundException;


@Service
public class EventService {
	@Autowired
	CityRepository cityRepository;
	
	@Autowired
	EventRepository repository;
	
	@Transactional(readOnly = true)
	public List<EventDTO> findAllPaged() {
		List<Event> list = repository.findAll(Sort.by("name"));
		return list.stream().map(x -> new EventDTO(x)).collect(Collectors.toList());
	}
	
	@Transactional
	public EventDTO insert(EventDTO dto) {
		Event entity = new Event();
		entity.setName(dto.getName());
		entity = repository.save(entity);
		return new EventDTO(entity);
	}
	
	@Transactional
	public EventDTO update(EventDTO dto, Long id) {
		try {
			City city = cityRepository.findById(dto.getCityId()).get();
			Event entity = repository.getReferenceById(id);
			entity.setName(dto.getName());
			entity.setDate(dto.getDate());
			entity.setUrl(dto.getUrl());
			entity.setCity(city);
			entity = repository.save(entity);
			return new EventDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
		
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
		try {
			repository.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
}

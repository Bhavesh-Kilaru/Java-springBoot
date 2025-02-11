package com.demo.cachingservice.controller;

import java.util.List;

import com.demo.cachingservice.model.Person;
import com.demo.cachingservice.repository.PersonRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.cachingservice.service.InMemoryCacheService;

@RestController
@RequestMapping("/api/v1/persons")
public class CacheController {
	private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

	private final InMemoryCacheService cacheService;

	@Autowired
	// Constructor injection in controller
	public CacheController(InMemoryCacheService cacheService) {
		this.cacheService = cacheService;
	}

	// method to get all entities
	@GetMapping("/")
	public List<Person> getAllEntities() {
		try {
			return cacheService.getAll();
		} catch (Exception e) {
			logger.error("Error retrieving all entities", e);
			throw new RuntimeException("Error retrieving all entities", e);
		}
	}

	// method to get all entities that are in cache

	// method to get entity
	@GetMapping("/{id}")
	public Person getEntity(@PathVariable String id) {
		try {
			return cacheService.get(id);
		} catch (Exception e) {
			logger.error("Error retrieving entity with ID: {}", id, e);
			throw new RuntimeException("Error retrieving entity", e);
		}
	}

	@GetMapping("/cache")
	public List<Person> getAllEntitiesFromCache() {
		try {
			return cacheService.getAllFromCache();
		} catch (Exception e) {
			logger.error("Error retrieving all entities From Cache", e);
			throw new RuntimeException("Error retrieving all entities From Cache", e);
		}
	}

	// method to add new entity
	@PostMapping("/")
	public String addEntity(@RequestParam String Id, @RequestParam String firstName, @RequestParam String lastName) {
		try {
			String id = cacheService.add(Id, firstName, lastName);
			logger.info("Successfully added entity. ID: {}", id);
			return id;
		} catch (Exception e) {
			logger.error("Error adding entity", e);
			throw new RuntimeException("Error adding entity", e);
		}
	}

	// method to remove all entities
	@DeleteMapping("/")
	public String removeAllEntities() {
		try {
			String msg = cacheService.removeAll();
			logger.info("Successfully removed all entities.");
			return msg;
		} catch (Exception e) {
			logger.error("Error removing all entities", e);
			throw new RuntimeException("Error removing all entities", e);
		}
	}

	// method to remove an entity
	@DeleteMapping("/{id}")
	public String removeEntity(@PathVariable String id) {
		try {
			String msg = cacheService.remove(id);
			logger.info("Successfully removed entity with ID: {}", id);
			return msg;
		} catch (Exception e) {
			logger.error("Error removing entity with ID: {}", id, e);
			throw new RuntimeException("Error removing entity", e);
		}
	}

	// method to clear cache
	@DeleteMapping("/cache")
	public String clearCache() {
		try {
			String msg = cacheService.clear();
			logger.info("Successfully cleared cache.");
			return msg;
		} catch (Exception e) {
			logger.error("Error clearing cache", e);
			throw new RuntimeException("Error clearing cache", e);
		}
	}
}

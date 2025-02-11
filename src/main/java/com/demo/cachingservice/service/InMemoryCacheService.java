package com.demo.cachingservice.service;

import com.demo.cachingservice.model.Person;
import com.demo.cachingservice.repository.PersonRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class InMemoryCacheService {

	private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

	// variable to store the maximum cache size
	private final int maxSize;

	// hash map to implement the Cache
	private final Map<String, Person> cache;

	// hash map to track the usage of elements
	private final Map<String, AtomicInteger> usageCount;

	// initializing the repository
	@Autowired
	private PersonRepository repository;

	// default constructor
	public InMemoryCacheService() {

		// accepting input from user to determine the cache size
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter the size of cache you want: ");
		this.maxSize = scanner.nextInt();
		scanner.close();
		this.cache = new ConcurrentHashMap<>();
		this.usageCount = new ConcurrentHashMap<>();
	}

	// method to add entity
	public synchronized String add(String id, String firstName, String lastName) {

		try {
			
			//checking with id already exists
			if(this.checkIfIdExists(id)) {
				return "Entity with id " + id + " already exists in cache";
			}
			
			// checking if cache size is exceeded
			if (cache.size() >= maxSize) {
				logger.info("Cache size exceeded. Evicting least-used entity.");
				evictLeastUsed();
			}
			Person person = new Person(id, firstName, lastName);

			// adding element to the Cache
			cache.put(id, person);
			usageCount.put(id, new AtomicInteger(1));
			logger.info("Added entity to cache: {}", person.getId());
			return "Entity with id " + id + " inserted to cache";
		} catch (Exception e) {
			logger.error("Error inserting entity with ID: {}", id, e);
			throw new RuntimeException("Error inserting entity", e);
		}
	}

	// method to get an entity
	public Person get(String id) {
		try {
			// checking for entity in cache
			Person entity = cache.get(id);

			// checking for entity in database if not found in cache
			if (entity == null) {

				// Fetch from database if not found in cache
				logger.info("Entity not found in cache. Checking from database: {}", id);
				entity = repository.findById(id).orElse(null);
				
				// moving the entity from data base to cache and evicting the least used one
				if (entity != null) {
					if(cache.size()== this.maxSize) {
						this.evictLeastUsed();
					}
					cache.put(id, entity);
				}
				
			}

			// incrementing the count
			if (entity != null) {
				usageCount.computeIfAbsent(id,  k -> new AtomicInteger(0)).incrementAndGet();
//				String output = usageCount.entrySet().stream()
//					    .map(entry -> entry.getKey() + " -> " + entry.getValue().get())
//					    .collect(Collectors.joining(", ", "{", "}"));
//					System.out.println(output);
				logger.info("Retrieved entity: {}", id);
			} else {
				logger.info("Entity not found in cache and database: {}", id);
			}

			return entity;
		} catch (Exception e) {
			logger.error("Error retrieving entity with ID: {}", id, e);
			throw new RuntimeException("Error retrieving entity", e);
		}
	}

	// method to list all entities
	public List<Person> getAll() {
		try {
			// Retrieve entries from the cache
			List<Person> allEntities = new ArrayList<>(cache.values());

			// Retrieve entries from the repository
			List<Person> repositoryEntities = repository.findAll();

			// adding the entities from repository
			allEntities.addAll(repositoryEntities.stream()
					.filter(entity -> !cache.containsKey(entity.getId())) // Check if the cache already has the entity
					.collect(Collectors.toList()));

			logger.info("Retrieved all entities. Total: {}", allEntities.size());
			return allEntities;
		} catch (Exception e) {
			logger.error("Error retrieving all entities", e);
			throw new RuntimeException("Error retrieving all entities", e);
		}
	}

	// method to list all entities from cache
	public List<Person> getAllFromCache() {
		try {
			List<Person> allEntities = new ArrayList<>(cache.values());

			logger.info("Retrieved all entities from cache. Total: {}", allEntities.size());
			return allEntities;
		} catch (Exception e) {
			logger.error("Error retrieving all entities from cache", e);
			throw new RuntimeException("Error retrieving all entities from cache", e);
		}
	}

	// removing entity from cache or database
	public synchronized String remove(String id) {
		try {
			Person removed_id = cache.remove(id);

			// checking in database
			if (removed_id == null) {
				logger.info("Entity with id {} not found in cache. Checking in Datbase", id);
				try {
					if(repository.existsById(id)) {
					repository.deleteById(id);
					usageCount.remove(id);
					logger.info("Removed entity with ID: {} from database", id);
					return "entity with ID: " + id + " deleted from database";
					}
					else {
						logger.warn("Entity with ID: {} not found in database", id);
					    return "Entity with ID: " + id + " not found in cache and database";
					}
				} catch (Exception e) {
					logger.info("Entity with ID: {} doesnt found in cache and database", id);
					return "entity with ID: " + id + " not found in cache and database";
				}

			} else {
				logger.info("Entity with ID: {} removed from cache", id);
				return "Removed entity with ID: " + id + " from cache";
			}
		} catch (Exception e) {
			logger.error("Error removing entity with ID: {}", id, e);
			throw new RuntimeException("Error removing entity", e);
		}
	}

	// method to remove all from cache and database
	public synchronized String removeAll() {
		try {
			cache.clear(); // clearing all the cache
			usageCount.clear(); // clearing the usage count
			repository.deleteAll(); // cleaning the database
			logger.info("Removed all entities from cache and database.");
			return "removed all entities";
		} catch (Exception e) {
			logger.error("Error removing all entities", e);
			throw new RuntimeException("Error removing all entities", e);
		}
	}

	// method to clear cache
	public String clear() {
		try {
			cache.clear(); // clearing all the cache
			usageCount.clear(); // clearing the usage count
			logger.info("Cleared all entities from cache.");
			return "removed all entities from cache";
		} catch (Exception e) {
			logger.error("Error clearing cache", e);
			throw new RuntimeException("Error clearing cache", e);
		}
	}

	// method to get the least used message
	private void evictLeastUsed() {
		try {
			// fetching the ID of least accessed element
			String leastUsedId = usageCount.entrySet().stream().min(Comparator.comparingInt(e -> e.getValue().get()))
					.map(Map.Entry::getKey).orElse(null);

			// removing from cache
			if (leastUsedId != null) {
				Person evictedPerson = cache.remove(leastUsedId);
				usageCount.remove(leastUsedId);
				logger.info("removed entity with id {} from cache", leastUsedId);

				// inserting to database
				if (evictedPerson != null) {
					repository.save(evictedPerson);
					logger.info("Evicted least-used entity with ID: {} from cache and inserted in database",
							leastUsedId);
				}
			}

		} catch (Exception e) {
			logger.error("Error evicting least-used entity", e);
			throw new RuntimeException("Error evicting least-used entity", e);
		}
	}

	// returning cache size
	public int getCacheSize() {
		return this.maxSize;
	}
	
	//checking if ID already exists
	public boolean checkIfIdExists(String id) {
		try {
			// checking for entity in cache
			Person entity = cache.get(id);

			// checking for entity in database if not found in cache
			if (entity == null) {
				// Fetch from database if not found in cache
				entity = repository.findById(id).orElse(null);
			}

			// incrementing the count
			if (entity != null) {
				return true;
			} 
			return false;
		} catch (Exception e) {
			logger.error("Error while checking for ID: {}", id, e);
			throw new RuntimeException("Error checking entity", e);
		}
	}
}

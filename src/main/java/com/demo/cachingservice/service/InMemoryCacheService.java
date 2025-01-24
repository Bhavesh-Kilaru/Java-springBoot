package com.demo.cachingservice.service;

import com.demo.cachingservice.model.PersonEntity;
import com.demo.cachingservice.repository.PersonEntityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
//import java.util.UUID;
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

    private final int maxSize;
    private final Map<String, PersonEntity> cache;
    private final Map<String, AtomicInteger> usageCount;
    @Autowired
    private PersonEntityRepository repository;
    //private final Map<String, PersonEntity> database; // Simulated database
    
    //default constructor
    public InMemoryCacheService() {
    	Scanner scanner = new Scanner(System.in);
    	System.out.print("Enter the size of cache you want: ");
    	this.maxSize = scanner.nextInt();
    	// this.maxSize = MaxSize;
        this.cache = new ConcurrentHashMap<>();
        this.usageCount = new ConcurrentHashMap<>();
        // this.database = new ConcurrentHashMap<>();
    }

    //method to add entity
    public synchronized String add(String id, String firstName, String lastName) {
    	
    	try {
        	
            if (cache.size() >= maxSize) {
                logger.info("Cache size exceeded. Evicting least-used entity.");
                evictLeastUsed();
            }
            //String id = UUID.randomUUID().toString();
            PersonEntity entity = new PersonEntity(id, firstName, lastName);
            cache.put(entity.getId(), entity);
            // database.put(entity.getId(), entity);
            usageCount.put(entity.getId(), new AtomicInteger(1));
            logger.info("Added entity to cache: {}", entity.getId());
            return "Entity with id "+id+" inserted to cache";
    	}
    	catch (Exception e) {
    		logger.error("Error inserting entity with ID: {}", id, e);
            throw new RuntimeException("Error inserting entity", e);
    	}
    }
    
    // method to get an entity
    public PersonEntity get(String id) {
        try {
        	//checking for entity in cache
        	PersonEntity entity = cache.get(id);
        	
        	//checking for entity in database if not found in cache
        	if(entity == null) {
        		// Fetch from database if not found in cache
                logger.info("Entity not found in cache. Checking from database: {}", id);
                
        		entity = repository.findById(id).orElse(null);
        	}
        	
        	//incrementing the count
            if (entity != null) {
                usageCount.get(id).incrementAndGet();
                logger.info("Retrieved entity: {}", id);
            }
            else {
            	logger.info("Entity not found in cache and database: {}", id);
            }
            
            return entity;
        } 
        	catch (Exception e) {
            logger.error("Error retrieving entity with ID: {}", id, e);
            throw new RuntimeException("Error retrieving entity", e);
        }
    }

    //method to list all entities
    public List<PersonEntity> getAll() {
        try {
        	// Retrieve entries from the cache
            List<PersonEntity> allEntities = new ArrayList<>(cache.values());
            
            // Retrieve entries from the repository
            List<PersonEntity> repositoryEntities = repository.findAll();
            
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
    
  //method to list all entities from cache
    public List<PersonEntity> getAllFromCache() {
        try {
            List<PersonEntity> allEntities = new ArrayList<>(cache.values());
            
            logger.info("Retrieved all entities from cache. Total: {}", allEntities.size());
            return allEntities;
        } catch (Exception e) {
            logger.error("Error retrieving all entities from cache", e);
            throw new RuntimeException("Error retrieving all entities from cache", e);
        }
    }

    //removing entity from cache or database
    public synchronized String remove(String id) {
        try {
        	PersonEntity removed_id = cache.remove(id);
        	
        	//checking in database
            if (removed_id == null) {
            	logger.info("Entity with id {} not found in cache. Checking in Datbase", id);
            	try {
	            	repository.deleteById(id);
	                usageCount.remove(id);
	                logger.info("Removed entity with ID: {} from database", id);
	                return "entity with ID: "+id+ " deleted from database";
            	} catch(Exception e) {
            		logger.info("Entity with ID: {} doesnt found in cache and database", id);
                    return "entity with ID: "+id+ " not found in cache and database";
            	}
                
            } else {
                logger.warn("Entity with ID: {} removed from cache", id);
                return "Removed entity with ID: "+id+ " from cache";
            }
        } catch (Exception e) {
            logger.error("Error removing entity with ID: {}", id, e);
            throw new RuntimeException("Error removing entity", e);
        }
    }

    //method to remove all from cache and database
    public synchronized String removeAll() {
        try {
            cache.clear();
            usageCount.clear();
            repository.deleteAll();
            logger.info("Removed all entities from cache and database.");
            return "removed all entities";
        } catch (Exception e) {
            logger.error("Error removing all entities", e);
            throw new RuntimeException("Error removing all entities", e);
        }
    }

    //method to clear cache
    public String clear() {
        try {
            cache.clear();
            usageCount.clear();
            logger.info("Cleared all entities from cache.");
            return "removed all entities from cache";
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
            throw new RuntimeException("Error clearing cache", e);
        }
    }

    //method to get the least used message
    private void evictLeastUsed() {
        try {
            String leastUsedId = usageCount.entrySet()
                    .stream()
                    .min(Comparator.comparingInt(e -> e.getValue().get()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            //removing from cache
            if (leastUsedId != null) {
                PersonEntity evictedEntity = cache.remove(leastUsedId);
                usageCount.remove(leastUsedId);
                logger.info("removed entity with id {} from cache", leastUsedId);
                
                //inserting to database
                if (evictedEntity != null) {
                	repository.save(evictedEntity);
                    logger.info("Evicted least-used entity with ID: {} from cache and inserted in database", leastUsedId);
                }
            }
            
	        } catch (Exception e) {
	            logger.error("Error evicting least-used entity", e);
	            throw new RuntimeException("Error evicting least-used entity", e);
	        }
    }
    
    // returning cache size
    public int getCacheSize(){
    	return this.maxSize;
    }
}

package com.demo.cachingservice.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
//import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.demo.cachingservice.model.PersonEntity;
import com.demo.cachingservice.repository.PersonEntityRepository;

public class InMemoryCacheService {

	private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

    private final int maxSize;
    private final Map<String, PersonEntity> cache;
    private final Map<String, AtomicInteger> usageCount;
    @Autowired
    private PersonEntityRepository repository;
    //private final Map<String, PersonEntity> database; // Simulated database
    
    public InMemoryCacheService(int MaxSize) {
    	this.maxSize = MaxSize;
        this.cache = new ConcurrentHashMap<>();
        this.usageCount = new ConcurrentHashMap<>();
//        this.database = new ConcurrentHashMap<>();
        this.repository = repository;
        if(this.repository == null) {
	    	logger.error("Repository Not Initialized");
	     }
	    else {
	    	logger.info("repository Initiated succesully");
	    }
    }
    
//    @Autowired
//	public void setRepository(PersonEntityRepository repository) {
//    	logger.info("In constructor");
//    	this.repository = repository;
//	    if(this.repository == null) {
//	    	logger.error("Repository Not Initialized");
//	     }
//	    else {
//	    	logger.info("repository Initiated succesully");
//	    }
//	}
    
//    public void initializeCache(int MaxSize){
//    	this.maxSize = MaxSize;
//    	
//    }


    public synchronized String add(String id, String firstName, String lastName) {
    	
    	if(this.repository == null) {
	    	logger.error("Repository Not Initialized");
	     }
	    else {
	    	logger.info("repository Initiated succesully");
	    }
    	
        if (cache.size() >= maxSize) {
            logger.info("Cache size exceeded. Evicting least-used entity.");
            evictLeastUsed();
        }
        //String id = UUID.randomUUID().toString();
        PersonEntity entity = new PersonEntity(id, firstName, lastName);
        cache.put(entity.getId(), entity);
//        database.put(entity.getId(), entity);
        usageCount.put(entity.getId(), new AtomicInteger(1));
        logger.info("Added entity to cache: {}", entity.getId());
        return id;
    }

    public PersonEntity get(String id) {
        try {
        	PersonEntity entity = cache.get(id);
            if (entity != null) {
                usageCount.get(id).incrementAndGet();
                logger.info("Retrieved entity from cache: {}", id);
                return entity;
            }
            // Fetch from database if not found in cache
            logger.info("Entity not found in cache. Retrieving from database: {}", id);
            return repository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error retrieving entity with ID: {}", id, e);
            throw new RuntimeException("Error retrieving entity", e);
        }
    }

    public List<PersonEntity> getAll() {
        try {
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

    public synchronized void remove(String id) {
        try {
            if (cache.remove(id) != null) {
            	repository.deleteById(id);
                usageCount.remove(id);
                logger.info("Removed entity with ID: {}", id);
            } else {
                logger.warn("Entity with ID: {} not found in cache or database", id);
            }
        } catch (Exception e) {
            logger.error("Error removing entity with ID: {}", id, e);
            throw new RuntimeException("Error removing entity", e);
        }
    }

    public synchronized void removeAll() {
        try {
            cache.clear();
            usageCount.clear();
            repository.deleteAll();
            logger.info("Removed all entities from cache and database.");
        } catch (Exception e) {
            logger.error("Error removing all entities", e);
            throw new RuntimeException("Error removing all entities", e);
        }
    }

    public void clear() {
        try {
            cache.clear();
            usageCount.clear();
            logger.info("Cleared all entities from cache.");
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
            throw new RuntimeException("Error clearing cache", e);
        }
    }

    private void evictLeastUsed() {
        try {
            String leastUsedId = usageCount.entrySet()
                    .stream()
                    .min(Comparator.comparingInt(e -> e.getValue().get()))
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (leastUsedId != null) {
                PersonEntity evictedEntity = cache.remove(leastUsedId);
                usageCount.remove(leastUsedId);
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
}

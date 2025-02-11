package com.demo.cachingservice;

import com.demo.cachingservice.model.Person;
import com.demo.cachingservice.service.InMemoryCacheService;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8081")
@TestMethodOrder(OrderAnnotation.class)
class CachingserviceApplicationTests {

	@Autowired
	private InMemoryCacheService cacheService;

	//test to check the input cache size
	@Test
	@Order(1)
	void testMaxCacheSize() {
		// Arrange: Simulate user input for cache size
		String simulatedInput = "5"; // Simulated input for maxSize
		ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
		System.setIn(inputStream); // Redirect System.in to simulated input

		// Act: Create an instance of InMemoryCacheService
		InMemoryCacheService cacheService = new InMemoryCacheService();

		// Assert: Verify the maxSize is set correctly
		assertEquals(5, cacheService.getCacheSize());

		// Clean up: Reset System.in to its original state
		System.setIn(System.in);
	}

	// testing the functionality to add elements
	// the functionality to get an entity with a particular id is also tested
	@Test
	@Order(2)
	void testgetElement() {
		System.out.println(cacheService.getCacheSize());
		// Act: Post the entities to the API
		cacheService.add("1", "John", "Doe");
		cacheService.add("2", "Alice", "Johnson");
		cacheService.add("3", "Jane", "Smith");
		cacheService.add("4", "Miranda", "Bailey");

		Person person = cacheService.get("3");
		assertEquals("3", person.getId());

	}
	
	// testing to check inserting values with same id
	@Test
	@Order(3)
	void testInsertingExistingID() {
		System.out.println(cacheService.getCacheSize());
		// Act: Post the entities to the API
		String msg = cacheService.add("2", "Joe", "Wilson");
		
		assertEquals("Entity with id 2 already exists in cache", msg);

	}

	// Testing to get all entities functionality
	//this return from both cache and database
	@Test
	@Order(4)
	void testGetAllElement() {

		List<Person> persons = cacheService.getAll();
		System.out.println(persons.size());
		assertEquals(4, persons.size());

	}

	// Testing to get all entities from cache functionality
	//this return entities from cache
	@Test
	@Order(5)
	void testGetAllElementFromcache() {
		List<Person> persons = cacheService.getAllFromCache();
		assertEquals(3, persons.size());
	}

	// Testing to delete all entities from cache functionality
	// the below functionality should keep entities that are in database
	@Test
	@Order(6)
	void testClearCache() {
		cacheService.clear();
		List<Person> persons_in_cache = cacheService.getAllFromCache();
		assertEquals(0, persons_in_cache.size());
	}
	
	// testing the existence of database after deleting entire cache
	@Test
	@Order(7)
	void testEntityIndataBase() {

		List<Person> persons_after_cache_clearence = cacheService.getAll();
		assertEquals(1, persons_after_cache_clearence.size());
	}

	// Testing delete functionality
	// to remove an entity with particular id
	@Test
	@Order(8)
	void testDeleteById() {
		System.out.println("cache size" + cacheService.getCacheSize());
		// Act: Post the entities to the API
		cacheService.add("5", "John", "Doe");
		cacheService.add("6", "Alice", "Johnson");
		cacheService.add("7", "Jane", "Smith");
		cacheService.add("8", "Miranda", "Bailey");

		String msg = cacheService.remove("8");

		// // Assert: Verify that the entity is null
		assertEquals(msg, "Removed entity with ID: 8 from cache");
	}

	// Testing delete functionality
	// to check whether the application is able to delete all entities
	@Test
	@Order(9)
	void testDeleteAllElements() {

		cacheService.removeAll();
		List<Person> persons_after_deleting = cacheService.getAll();
		assertEquals(0, persons_after_deleting.size());

	}
	
	@Test
	@Order(10)
	void testCacheSizeLimit() {
	    // Act: Add more elements than the cache size
	    cacheService.add("9", "Tom", "Hanks");
	    cacheService.add("10", "Emma", "Watson");
	    cacheService.add("11", "Robert", "Downey Jr.");
	    cacheService.add("12", "Scarlett", "Johansson");

	    // Assert: Check the cache size does not exceed the limit
	    assertEquals(3, cacheService.getAllFromCache().size());  // Assuming FIFO/LRU eviction
	}
	
	@Test
	@Order(11)
	void testCacheEviction() {
	    // Act: Add elements to fill the cache
	    cacheService.add("13", "Chris", "Evans");
	    cacheService.add("14", "Mark", "Ruffalo");
	    cacheService.add("15", "Jeremy", "Renner");

	    // Add one more element to exceed the cache size
	    cacheService.add("16", "Paul", "Rudd");

	    // Assert: Check if the oldest item (id "13") is evicted
	    // Assert: Check if the first element is moved to the database
	    List<Person> persons_in_cache = cacheService.getAllFromCache();
	    assertTrue(persons_in_cache.stream().noneMatch(person -> person.getId().equals("13")));

	    Person remainingPerson = cacheService.get("14");
	    assertNotNull(remainingPerson);  // "14" should still exist in the cache
	}
	
	@Test
	@Order(12)
	void testGetNonExistentEntity() {
	    // Attempt to retrieve an entity that doesn't exist
	    Person person = cacheService.get("99");
	    assertNull(person);  // Should return null if the entity is not found in the cache
	}
	
	@Test
	@Order(13)
	void testAddingAndRetrievingMultipleEntities() {
	    // Add multiple entities
	    cacheService.add("17", "Chris", "Hemsworth");
	    cacheService.add("18", "Natalie", "Portman");

	    // Retrieve each entity
	    Person person1 = cacheService.get("17");
	    assertEquals("Chris", person1.getFirstName());

	    Person person2 = cacheService.get("18");
	    assertEquals("Natalie", person2.getFirstName());
	}
	
	// to check whether database is persistent
	@Test
	@Order(14)
	void testCacheEvictionAndDatabaseUpdate() {
		cacheService.removeAll();
		
	    // Act: Add 4 elements to the cache
	    cacheService.add("1", "John", "Doe");
	    cacheService.add("2", "Alice", "Johnson");
	    cacheService.add("3", "Jane", "Smith");
	    cacheService.add("4", "Miranda", "Bailey"); // Adding the 4th element should trigger eviction of the first element

	    // Assert: Check if the first element is moved to the database
	    List<Person> persons_in_cache = cacheService.getAllFromCache();
	    assertTrue(persons_in_cache.stream().noneMatch(person -> person.getId().equals("1")));  // ID "1" should no longer be in cache
	    
	    //fetching the element from database
	    cacheService.get("1"); 

	    // Act: Clear the cache
	    cacheService.clear();

	    // Assert: After clearing the cache, the database should contain the last two evicted entities
	    List<Person> persons_in_db = cacheService.getAll();  // This should return the two entities in the database
	    assertEquals(2, persons_in_db.size());  // There should be 2 entities in the database (id 2 and id 1)
	}
	
	// checking whether the element is moved from database to Cache
	@Test
	@Order(15)
	void checkEntityInCache() {
		//fetching the element from database
	    cacheService.get("1"); 
	    
	    List<Person> persons_in_cache = cacheService.getAll(); 
	    boolean existsInCache = persons_in_cache.stream().anyMatch(person -> person.getId().equals("1"));
	    assertTrue(existsInCache, "Entity with ID 1 should now be in the cache");
	}
	
	// checking for an irrelevant element
	@Test
	@Order(16)
	void testRemoveNonExistentEntity() {
	    // Try removing an entity that doesn't exist
	    String msg = cacheService.remove("100");
	    assertEquals("Entity with ID: 100 not found in cache and database", msg);
	}

}

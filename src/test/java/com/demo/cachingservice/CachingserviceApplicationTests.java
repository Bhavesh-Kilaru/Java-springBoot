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
import static org.junit.jupiter.api.Assertions.assertNull;

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
	// Testing delete functionality
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

		List<Person> persons_after_cache_clearence = cacheService.getAll();
		assertEquals(1, persons_after_cache_clearence.size());
	}

	// Testing delete functionality
	// to remove an entity with particular id
	@Test
	@Order(7)
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
	@Order(8)
	void testDeleteAllElements() {

		cacheService.removeAll();
		List<Person> persons_after_deleting = cacheService.getAll();
		assertEquals(0, persons_after_deleting.size());

	}
}

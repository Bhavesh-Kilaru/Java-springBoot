package com.demo.cachingservice;

import com.demo.cachingservice.model.PersonEntity;
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

	@Test
	void contextLoads() {
	}

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

	// testing get functionalities and clearing cache
	// Testing delete functionality
	@Test
	@Order(2)
	void testgetElement() {
		System.out.println(cacheService.getCacheSize());
		// Act: Post the entities to the API
		cacheService.add("1", "John", "Doe");
		cacheService.add("2", "Alice", "Johnson");
		cacheService.add("3", "Jane", "Smith");
		cacheService.add("4", "Miranda", "Bailey");

		PersonEntity person = cacheService.get("3");
		assertEquals("3", person.getId());

	}

	// Testing to get all elements functionality
	@Test
	@Order(3)
	void testGetAllElement() {

		List<PersonEntity> persons = cacheService.getAll();
		System.out.println(persons.size());
		assertEquals(4, persons.size());

	}

	// Testing to get all elemnets from cache functionality
	@Test
	@Order(4)
	void testGetAllElementFromcache() {
		List<PersonEntity> persons = cacheService.getAllFromCache();
		assertEquals(3, persons.size());
	}

	// Testing to delete all elements from cache functionality
	@Test
	@Order(5)
	void testClearCache() {
		cacheService.clear();
		List<PersonEntity> persons_in_cache = cacheService.getAllFromCache();
		assertEquals(0, persons_in_cache.size());

		List<PersonEntity> persons_after_cache_clearence = cacheService.getAll();
		assertEquals(1, persons_after_cache_clearence.size());
	}

	// Testing delete functionality
	@Test
	@Order(6)
	void testDeleteById() {
		System.out.println("cache size" + cacheService.getCacheSize());
		// Act: Post the entities to the API
		cacheService.add("5", "John", "Doe");
		cacheService.add("6", "Alice", "Johnson");
		cacheService.add("7", "Jane", "Smith");
		cacheService.add("8", "Miranda", "Bailey");

		cacheService.remove("8");
		PersonEntity entityCheck = cacheService.get("8");

		// // Assert: Verify that the entity is null
		assertNull(entityCheck, "Entity with ID '4' should not exist in the cache or database");

		cacheService.removeAll();
		List<PersonEntity> persons_after_deleting = cacheService.getAll();
		assertEquals(0, persons_after_deleting.size());

	}

	// Testing delete functionality
	@Test
	@Order(7)
	void testDeleteAllElements() {

		cacheService.removeAll();
		List<PersonEntity> persons_after_deleting = cacheService.getAll();
		assertEquals(0, persons_after_deleting.size());

	}
}

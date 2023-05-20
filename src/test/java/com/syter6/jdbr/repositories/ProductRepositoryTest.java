package com.syter6.jdbr.repositories;

import com.syter6.jdbr.models.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest {

	@Test
	public void getByReturnsProductWhenFound() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getBy("name", "Product 1");
		repos.truncate();

		assertNotNull(product);
		assertEquals("Product 1", product.name);
	}

	@Test
	public void getByReturnsNullWhenNotFound() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getBy("name", "Product 2");
		repos.truncate();

		assertNull(product);
	}

	@Test
	public void getByIdReturnsProductWhenFoundWithStringPk() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getById("1");
		repos.truncate();

		assertNotNull(product);
		assertEquals("Product 1", product.name);
	}

	@Test
	public void getByIdReturnsProductWhenFoundWithIntPk() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getById(1);
		repos.truncate();

		assertNotNull(product);
		assertEquals("Product 1", product.name);
	}

	@Test
	public void getByIdReturnsNullWhenNotFound() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getById(2);
		repos.truncate();

		assertNull(product);
	}

	@Test
	public void getByIdReturnsNullWhenPassingNull() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product product = repos.getById(null);
		repos.truncate();

		assertNull(product);
	}

	@Test
	public void createProductsGivesTrue() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();

		boolean result1 = repos.create(new Product(1, "Product 1"));
		boolean result2 = repos.create(new Product(2, "Product 2"));
		repos.truncate();

		assertTrue(result1 && result2);
	}

	@Test
	public void createProductWithNullGivesFalse() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();

		boolean result = repos.create(null);
		repos.truncate();

		assertFalse(result);
	}

	@Test
	public void createProductWithUsedPKReturnsFalse() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();

		repos.create(new Product(1, "Product 1"));
		boolean result = repos.create(new Product(1, "Product 2"));
		repos.truncate();

		assertFalse(result);
	}

	@Test
	public void updateProductReturnsTrueWhenValid() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product prod = repos.getById(1);
		prod.name = "Product 1 [low stock!]";
		boolean result = repos.update(prod);
		repos.truncate();

		assertTrue(result);
	}

	@Test
	public void updateProductReturnsFalseWhenInvalid() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product prod = repos.getById(1);
		prod.name = null;
		boolean result = repos.update(prod);
		repos.truncate();

		assertFalse(result);
	}

	@Test
	public void updateProductChangesData() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		Product prod = repos.getById(1);
		prod.name = "Product 1 [low stock]";
		repos.update(prod);

		Product new_prod = repos.getById(1);
		repos.truncate();

		assertEquals("Product 1 [low stock]", new_prod.name);
	}

	@Test
	public void deleteReturnsTrueWhenValid() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		Product product = new Product(1, "Product 1");
		repos.create(product);

		boolean result = repos.delete(product);
		repos.truncate();

		assertTrue(result);
	}

	@Test
	public void deleteReturnsFalseWhenInvalid() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		Product product = new Product(1, "Product 1");
		repos.create(product);

		product.num = 2;

		boolean result = repos.delete(product);
		repos.truncate();

		assertFalse(result);
	}

	@Test
	public void deleteRemovesTheItemFromTheDatabase() {
		ProductRepository repos = new ProductRepository();
		repos.truncate();
		repos.create(new Product(1, "Product 1"));

		repos.delete(new Product(1, "Product 1"));
		Product product = repos.getById(1);
		repos.truncate();

		assertNull(product);
	}

}
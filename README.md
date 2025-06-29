 # Proyect1

## Descripción
- Proyect1 es  un API REST con Spring Boot que permite gestionar un sistema  de e-commerce enfocado en productos y órdenes de compra.

## Requisitos
- Java 17 

## Instalación
-Repositorio:
git " https://github.com/Hinostrosaa/Proyect1.git "

## Contenido
####Estructura General
El proyecto es una aplicación Spring Boot que gestiona órdenes y productos. Está organizado en los siguientes paquetes principales:
-controller: Maneja las solicitudes HTTP y devuelve respuestas.
-model: Contiene las entidades Order y Product.
-repository: Interfaces para interactuar con la base de datos.
-service: Lógica de negocio y validaciones.
-exception: Manejo de excepciones personalizadas.
####Componentes Principales
#####1. Entidades (model)
Product:
Campos: id, name, description, price, stock, category.
Validaciones:
NotNull y @Size(min = 2) para el nombre.
NotNull para el precio.
Builder Pattern: Permite crear instancias de Product de manera más legible.

		  @Entity
		  public class Product {
			@Id
			@GeneratedValue(strategy = GenerationType.IDENTITY)
			private Long id;

		@NotNull(message = "El nombre es obligatorio y no puede estar vacío")
		@Size(min = 2, message = "El nombre debe tener al menos 2 caracteres y no puede estar vacío")
		private String name;

		private String description;

		@NotNull(message = "El precio es obligatorio y no puede estar vacío")
		private Double price;
		private Integer stock;
		private String category;


		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Integer getStock() {
			return stock;
		}
		public void setStock(Integer stock) {
			this.stock = stock;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		// Implementación del patrón Builder
		public static class Builder {
			private String name;
			private String description;
			private Double price;
			private Integer stock;
			private String category;

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder description(String description) {
				this.description = description;
				return this;
			}

			public Builder price(Double price) {
				this.price = price;
				return this;
			}

			public Builder stock(Integer stock) {
				this.stock = stock;
				return this;
			}

			public Builder category(String category) {
				this.category = category;
				return this;
			}

			public Product build() {
				Product product = new Product();
				product.name = this.name;
				product.description = this.description;
				product.price = this.price;
				product.stock = this.stock;
				product.category = this.category;
				return product;
			}
		}
	}

######Order:
Campos: id, client, date, status, products (relación ManyToMany con Product), total.
Validaciones:
NotNull para campos obligatorios.
Size(min = 1) para asegurar que la orden tenga al menos un producto.
	@Entity
	public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Se requiere Cliente para la orden")
    private String client;

    private LocalDate date;

    @NotNull(message = "Se requiere Estado para la orden")
    private String status;

    @NotNull(message = "Se requiere Productos para la orden")
    @Size(min = 1, message = "Se requiere almenos un producto")
    @ManyToMany
    @JoinTable(
        name = "order_products", 
        joinColumns = @JoinColumn(name = "order_id"), 
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
    private Double total;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getClient() {
        return client;
    }
    public void setClient(String client) {
        this.client = client;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public List<Product> getProducts() {
        return products;
    }
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    public Double getTotal() {
        return total;
    }
    public void setTotal(Double total) {
        this.total = total;
    }
}

#####2. Repositorios (repository)
######ProductRepository:
Métodos: findByNameContaining para búsqueda por nombre.

	@Repository
	public interface ProductRepository extends JpaRepository<Product, Long> {
		List<Product> findByNameContaining(String name);
	}

######OrderRepository:
Métodos: findByClient, findByStatus, para realizar consultas personalizadas en la base de datos.

	@Repository
	public interface OrderRepository extends JpaRepository<Order, Long> {
		List<Order> findByClient(String client);
		List<Order> findByStatus(String status);
	}


#####3. Servicios (service)
######ProductService:
CRUD completo para productos.
Maneja excepciones como ProductNotFoundException.

	@Service
	public class ProductService {
		@Autowired
		private ProductRepository productRepository;

		public Product createProduct(Product product) {
			return productRepository.save(product);
		}

		public List<Product> getProducts(String name) {
		if (name == null || name.isEmpty()) {
			return productRepository.findAll(); // Devuelve todos los productos si no se proporciona nombre
		}
		return productRepository.findByNameContaining(name); // Filtra productos por nombre
	}

		public Product getProductById(Long id) {
			return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
		}

		public Product updateProduct(Long id, Product product) {
			Product existingProduct = getProductById(id);
			existingProduct.setName(product.getName());
			existingProduct.setDescription(product.getDescription());
			existingProduct.setPrice(product.getPrice());
			existingProduct.setStock(product.getStock());
			existingProduct.setCategory(product.getCategory());
			return productRepository.save(existingProduct);
		}

		public void deleteProduct(Long id) {
			productRepository.deleteById(id);
		}
	}
######OrderService:
createOrder: Valida productos, calcula el total y guarda la orden.
getOrders: Filtra órdenes por cliente o estado.

	@Service
	public class OrderService {
		@Autowired
		private OrderRepository orderRepository;

		public Order createOrder(Order order) {
			if (order.getProducts() == null || order.getProducts().isEmpty()) {
			throw new IllegalArgumentException("Los productos no pueden ser nulos o vacíos");
		}
		// Calcular el total de la orden
		double total = order.getProducts().stream()
							.mapToDouble(Product::getPrice)
							.sum();
		order.setTotal(total); // Establecer el total en la orden

		// Guardar la orden en la base de datos
		return orderRepository.save(order);
	}

		public List<Order> getOrders(String client, String status) {
			if (client != null) {
				return orderRepository.findByClient(client);
			} else if (status != null) {
				return orderRepository.findByStatus(status);
			} else {
				return orderRepository.findAll();
			}
		}
	}

#####4. Controladores (controller)
######ProductController:
CRUD:
POST /products: Crea un producto.
GET /products: Lista productos (filtrable por nombre).
GET /products/{id}: Obtiene un producto por ID.
PUT /products/{id}: Actualiza un producto.
DELETE /products/{id}: Elimina un producto.

	@RestController
	@RequestMapping("/products")
	public class ProductController {
		@Autowired
		private ProductService productService;

		@PostMapping
		public ResponseEntity<Product> createProduct(@RequestBody @Valid Product product) {
			Product createdProduct = productService.createProduct(product);
			return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
		}

		@GetMapping
		public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String name) {
			List<Product> products = productService.getProducts(name);
			if (products.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Devuelve 204 No Content si no hay productos
			}
			return new ResponseEntity<>(products, HttpStatus.OK); // Devuelve los productos si existen
		}

		@GetMapping("/{id}")
		public ResponseEntity<Product> getProductById(@PathVariable Long id) {
			try {
				Product product = productService.getProductById(id);
				return new ResponseEntity<>(product, HttpStatus.OK);
			} catch (ProductNotFoundException e) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Devuelve 404 si no se encuentra el producto
			}
		}

		@PutMapping("/{id}")
		public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
			return new ResponseEntity<>(productService.updateProduct(id, product), HttpStatus.OK);
		}

		@DeleteMapping("/{id}")
		public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
			productService.deleteProduct(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

######OrderController:
POST /orders: Crea una orden con validación.
GET /orders: Lista órdenes (filtrable por cliente o estado).

	@RestController
	@RequestMapping("/orders")
	public class OrderController {
		@Autowired
		private OrderService orderService;

		@PostMapping
		public ResponseEntity<Order> createOrder(@RequestBody @Valid Order order) {
			try {
				Order createdOrder = orderService.createOrder(order); // Llama al servicio para crear la orden
				return new ResponseEntity<>(createdOrder, HttpStatus.CREATED); // Devuelve la respuesta con el código 201
			} catch (Exception e) {
				// En caso de error, devolver un 500 con el mensaje de error
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@GetMapping
		public ResponseEntity<List<Order>> getOrders(@RequestParam(required = false) String client, @RequestParam(required = false) String status) {
			return new ResponseEntity<>(orderService.getOrders(client, status), HttpStatus.OK);
		}
	}

#####5. Manejo de Excepciones (exception)
######GlobalExceptionHandler:
Maneja:
ProductNotFoundException (404 Not Found).
IllegalArgumentException (400 Bad Request para datos inválidos).
Excepcione (500 Internal Server Error).
	@ControllerAdvice
	public class GlobalExceptionHandler {
		@ExceptionHandler(ProductNotFoundException.class)
		public ResponseEntity<String> handleProductNotFound(ProductNotFoundException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
		}

		@ExceptionHandler(Exception.class)
		public ResponseEntity<String> handleGeneralError(Exception ex) {
			return new ResponseEntity<>("Internal Server Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		@ExceptionHandler(IllegalArgumentException.class)
		public ResponseEntity<String> handleInvalidOrderData(IllegalArgumentException ex) {
			return new ResponseEntity<>("Invalid order data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

######Excepciones personalizadas:
ProductNotFoundException.

	public class ProductNotFoundException extends RuntimeException {
		public ProductNotFoundException(String message) {
			super(message);
		}
	}
	

OrderNotFoundException (aun no lo emplemento en los controladores).

	package com.parcial.exception;

	public class OrderNotFoundException extends RuntimeException {
		public OrderNotFoundException(String message) {
			super(message);
		}
	}

####Conexion del Proyect1
######Crear un Producto (POST) Utilizando postman
######·POST /products

	{  
		"name": "Laptop HP",  
		"description": "Laptop de 15 pulgadas con 8GB RAM",  
		"price": 899.99,  
		"stock": 50,  
		"category": "Electrónicos"  
	}  

######--Respuesta Exitosa (201 Created):
	{  
		"id": 1,  
		"name": "Laptop HP",  
		"description": "Laptop de 15 pulgadas con 8GB RAM",  
		"price": 899.99,  
		"stock": 50,  
		"category": "Electrónicos"  
	}  
######Listar  un Producto
######·GET /products
######--Respuesta (200 OK):
	{  
		"id": 1,  
		"name": "Laptop HP",  
		"description": "Laptop de 15 pulgadas con 8GB RAM",  
		"price": 899.99,  
		"stock": 50,  
		"category": "Electrónicos"  
	} 
######Filtrar por nombre:
######·Endpoint: GET /product/1
######--Respuesta (200 OK)::
	{  
		"id": 1,  
		"name": "Laptop HP",  
		"description": "Laptop de 15 pulgadas con 8GB RAM",  
		"price": 899.99,  
		"stock": 50,  
		"category": "Electrónicos"  
	}  



###Fin

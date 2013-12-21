EntityDB
========

EntityDB is a simple, embedded NoSQL database. It's designed to use in desktop applications, and smaller, singlenode web applications. It's API is similar but not equal with the Google App Engine's Datastore API.

#Quick Start Guide

## Connecting to a database
The first step is to connect to a database file. For this you can use the EntityDB class like this:

    EntityDB edb = EntityDB.connect(new File("D:/test.edb"));
    
This will open the given file as a database, or create a new file if it isn't exists. Only one EntityDB instance can open the same file, because EntityDB locks the file exclusively. When you open a database file, EntityDB is indexing the database. This can take a while, depends on the size of the database.

## Getting DB instance
The DB class can be used to operate with the database. You can get as many DB instances as you want. You can think the DB instance as a transaction, but it isn't. The DB class is thread-safe, but it's a common practice to get a DB instance pre transaction.

You can get a DB instance like this:

    DB db = edb.getDB();
    
##Entity
The Entity class is represents a persistable object. It has an id, a kind, attributes, and a value.

###ID
The id of an entity is automatically genrated when you call the DB.put(Entity) method. Before the put method call, the id of the entity is -1. The ID of an entity is remains the same in the database, so you can safely store it in another Entitys.

###Kind
Every entity has a kind. This is a string, representing the kind of the entity like: "User", "Address", "Basket" or anithing you want.

###Attribute
Every entity can has zero or more attributes. Attributes are simple key-value pairs which are indexed. The key is a String like "username" or "password", or "dateOfBirth". The value of an attribute can be boolean, long, String.
Querying the database is done by attribute filters.

###Value
Every entity can has a value. This is a simple serializable Java object. The value isn't indexed, and you can't query the database based on values.

## Storing data
You can store entities in to the database throught the DB.put(Entity) method. The put method stores a new Entity in the database if the given entity is never stored, or updates the entity, if the given entity is already stored.

    // creating new User
    Entity ent = new Entity("User");
	ent.putAttribute("username", "John");
	ent.putAttribute("email", "john@example.com");
	ent.putAttribute("registerTime", System.currentTimeMillis());
	ent.putAttribute("points", 0);
	ent.setValue("Some value related to John");
		
	// storing new User
	db.put(ent);
	
	// getting John's id
	long johnId = ent.getId();
		
	// Updating the user
	ent.putAttribute("points", 10);
	ent.setValue("Changed value.");
		
	// updating the User
	db.put(ent);
    
## Getting data by ID
The simplest and fastest way to get data from the database is the DB.get(long) method which receives an id as a parameter and returns the Entity which holds the given id.

    Entity john = db.get(johnId);

## Querying the database
You can construct query filters based on attribute names and values. A query is allways returns entities from one defined kind.

### Simple filter
There are 4 kind of simple filter. LongFilterItem, BooleanFilterItem, StringFilterItem, NullFilterItem. You can define a filter using this 4 filteritem. You need to choose the perfect type, because StringFilterItem only filters attributes with string value, etc...

After you created your filteritem you must put it in a Filter object. The Filter object can be passed to the DB.query(Filter) method.

	// create the username FilterItem
	StringFilterItem usernameFilter = new StringFilterItem("username", StringFilterItem.EOperator.EQUALS, "john");
	// creating the filter and applying it to the "User" kind
	Filter filter = new Filter("User", usernameFilter);
	// query the database
	List<Entity> result = db.query(filter);
	// reading result
	Entity john = result.get(0);
	
String attribute value comparsion is case insensitive.

	
### Composite filters
There are some situations when you want to query using more than one attribute. In this case, the FilterGroupItem class can be used to squash more than one filters like this:

	// create the username FilterItem
	StringFilterItem usernameFilter = new StringFilterItem("username", StringFilterItem.EOperator.EQUALS, "john");
	// create point filter
	LongFilterItem pointFilter = new LongFilterItem("points", LongFilterItem.EOperator.SMALLER, 50);
	// put it in an AND group
	FilterGroupItem filterGroup = new FilterGroupItem(FilterGroupItem.EOperator.AND, usernameFilter, pointFilter);
	// creating the filter and applying it to the "User" kind
	Filter filter = new Filter("User", filterGroup);
	// query the database
	List<Entity> result = db.query(filter);
	// reading result
	Entity john = result.get(0);
	
Ofcourse you can put an another filtergroup inside a filtergroup.
	
## Remove elements
You can remove elements by id using the DB.remove(long) method.

	// removing john
	db.remove(johnId);
	
## Transactions
EntityDB does not implement any transaction logic. You need to implement your own with simple Java thread synchronization techniques.
But the DB is thread-safe, which means lots of read operations  can run at the same time from different threads, but only one write is allowed. When a write is running, no read is allowed. This is done using Java's ReentrantReadWriteLock.

## Closing the database
You don't need to close DB instances, but you need to close the EntityDB instance when your application is closing, or when you don't need it anymore.

	edb.close();



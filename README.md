EntityDB
========

EntityDB is a simple, embedded NoSQL database. It's designed to use in desktop applications, and smaller, singlenode web applications. It's API is similar but not equal with the Google App Engine's Datastore API.

#Quick Start Guide

## Connecting to a database
The first step is to connect to a database file. For this you can use the EntityDB class like this:

    EntityDB edb = EntityDB.connect(new File("D:/test.edb"));
    
This will open the given file as a database, or create a new file if it isn't exists. Only one EntityDB instance can open the same file, because EntityDB locks the file exclusively.

## Getting DB instance
The DB class can be used to operate with the database. You can get as many DB instances as you want. You can think the DB instance as a transaction, but it isn't. The DB class is thread-safe, but it's a common practice to get a DB instance pre transaction.

You can get a DB instance like this:

    DB db = edb.getDB();
    



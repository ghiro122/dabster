# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I would not refactor everything just to use the same pattern. For simple CRUD operations, Panache Active Record or a repository are both fine. For Warehouse, i'd keep the use cases and ports because the business logic is more complex and easier to test this way.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI first gives a clear contract and is useful when an API is shared with other teams. The downside is more generated code and a more complex build. Code first is simpler for small internal APIs. I'd choose the approach based on who uses the API.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I'd write unit tests for the main business rules and a smaller number of integration tests for database, HTTP and transaction behaviour. I would focus first on the riskiest parts, such as warehouse replacement and the Store synchronization after commit. Tests should be independent and every bug should get a regression test.
```
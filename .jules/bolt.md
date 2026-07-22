## 2024-07-22 - [Gson Unsafe Deserialization Field Initializer Bypass]
**Learning:** In Kotlin applications, Gson deserializes classes without a default (zero-argument) constructor by using JVM Unsafe allocation, completely bypassing field initializers inside the class body. Non-nullable fields initialized in the body will remain `null` in production, leading to unexpected NullPointerExceptions.
**Action:** Always use volatile private backing properties with lazy double-checked locking getters for transient fields of deserialized models, or ensure a default constructor exists.

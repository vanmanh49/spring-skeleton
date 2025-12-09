-- Test data for integration tests
-- Note: Passwords are bcrypt hashed versions of "testpassword"
-- H2 syntax for inserting with explicit IDs
INSERT INTO users (id, user_name, hashed_password) VALUES 
(1, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJwC'),
(2, 'adminuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJwC');

INSERT INTO roles (id, role_code, user_id) VALUES
(1, 'ADMINISTRATOR', 2),
(2, 'EDITOR', 1);


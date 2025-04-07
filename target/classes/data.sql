-- First insert into persons table (parent of users)
INSERT INTO persons (name, email, phone_number)
VALUES ('Admin User', 'admin@example.com', '123-456-7890');

-- Then insert into users table
INSERT INTO users (person_id, password, role, first_name, last_name)
VALUES (1, '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'ROLE_ADMIN', 'Admin', 'User');

-- Insert some sample cars
INSERT INTO car (make, model, type, transmission, fuel_type, price_per_day, image_url, owner, availability_status, location, insurance)
VALUES 
    ('Toyota', 'Camry', 'Sedan', 'Automatic', 'Petrol', 50.00, 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=500', 'George', true, 'New York', 'Full Coverage'),
    
    ('Honda', 'CR-V', 'SUV', 'Automatic', 'Petrol', 65.00, 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=500', 'Lily', true, 'Los Angeles', 'Full Coverage'),
    
    ('Tesla', 'Model 3', 'Sedan', 'Automatic', 'Electric', 80.00, 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=500', 'Abella', false, 'San Francisco', 'Full Coverage'),
    
    ('BMW', 'X5', 'SUV', 'Automatic', 'Diesel', 95.00, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=500', 'Eva', true, 'Chicago', 'Full Coverage'),
    
    ('Volkswagen', 'Golf', 'Hatchback', 'Manual', 'Petrol', 45.00, 'https://images.unsplash.com/photo-1471444928139-48c5bf5173f8?auto=format&fit=crop&w=500', 'Dani', true, 'Miami', 'Full Coverage'),
    
    ('Mercedes-Benz', 'C-Class', 'Sedan', 'Automatic', 'Petrol', 85.00, 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=500', 'Jhonny', true, 'Boston', 'Full Coverage'),
    
    ('Audi', 'Q7', 'SUV', 'Automatic', 'Diesel', 100.00, 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?auto=format&fit=crop&w=500', 'Jenny', true, 'Seattle', 'Full Coverage'),
    
    ('Ford', 'Mustang', 'Sports', 'Manual', 'Petrol', 120.00, 'https://images.unsplash.com/photo-1584345604476-8ec5e12e42dd?auto=format&fit=crop&w=500', 'Rhea', false, 'Las Vegas', 'Full Coverage'),
    
    ('Porsche', 'Cayenne', 'SUV', 'Automatic', 'Petrol', 150.00, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=500', 'Molly', true, 'Houston', 'Full Coverage');

-- Only add this if you need initial data
-- This will only run if spring.sql.init.mode=always                         
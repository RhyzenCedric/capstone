const express = require('express');
const mysql = require('mysql');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcrypt');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(bodyParser.json()); // Parse JSON request bodies

// Create a connection to the MySQL database
const db = mysql.createConnection({
    host: 'localhost', // Your database host
    user: 'root',      // Your database user
    password: '',      // Your database password
    database: 'phisherman' // Your database name
});

// Connect to the database
db.connect((err) => {
    if (err) {
        console.error('Error connecting to MySQL:', err);
        return;
    }
    console.log('Connected to MySQL database.');
});

app.post('/adminsignup', async (req, res) => {
    const { admin_username, admin_password } = req.body;

    // Check if the username already exists
    const checkIfExistsQuery = "SELECT * FROM admins WHERE admin_username = ?";
    const checkIfExistsValues = [admin_username];

    db.query(checkIfExistsQuery, checkIfExistsValues, async (err, results) => {
        if (err) {
            console.error('Error checking if user exists:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        // If a user with the provided username already exists, return an error
        if (results.length > 0) {
            return res.status(400).json({ error: 'Username already exists' });
        }

        // Hash the password before storing it
        const hashedPassword = await bcrypt.hash(admin_password, 10);

        // Proceed with registration
        const sql = "INSERT INTO admins (admin_username, admin_password) VALUES (?, ?)";
        const values = [admin_username, hashedPassword];

        db.query(sql, values, (err) => {
            if (err) {
                console.error('Error registering user:', err);
                return res.status(500).json({ error: 'Internal server error' });
            }
            return res.json({ message: 'Registration successful' });
        });
    });
});

app.post('/adminlogin', (req, res) => {
    const { admin_username, admin_password } = req.body;

    // Find the user by username
    const sql = "SELECT * FROM admins WHERE admin_username = ?";
    db.query(sql, [admin_username], async (err, results) => {
        if (err) {
            console.error('Error logging in:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        // Check if user exists
        if (results.length === 0) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const admin = results[0];
        // Compare the inputted password with the hashed password
        const match = await bcrypt.compare(admin_password, admin.admin_password);
        if (match) {
            return res.status(200).json({ message: 'Login successful' });
        } else {
            return res.status(401).json({ error: 'Invalid password' });
        }
    });
});

app.post('/usersignup', async (req, res) => {
    const { userUsername, userEmail, userPassword } = req.body;

    // Similar checks and logic as before
    const checkIfExistsQuery = "SELECT * FROM users WHERE userUsername = ?";
    db.query(checkIfExistsQuery, [userUsername], async (err, results) => {
        if (results.length > 0) {
            return res.status(400).json({ error: 'Username already exists' });
        }

        const hashedPassword = await bcrypt.hash(userPassword, 10);

        const sql = "INSERT INTO users (userUsername, userEmail, userPassword) VALUES (?, ?, ?)";
        const values = [userUsername, userEmail, hashedPassword];

        db.query(sql, values, (err) => {
            if (err) {
                return res.status(500).json({ error: 'Internal server error' });
            }
            res.json({ message: 'Registration successful' });
        });
    });
});

app.post('/userlogin', (req, res) => {
    const { userUsername, userPassword } = req.body;

    // Find the user by username
    const sql = "SELECT * FROM users WHERE userUsername = ?";
    db.query(sql, [userUsername], async (err, results) => {
        if (err) {
            console.error('Error logging in:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        // Check if user exists
        if (results.length === 0) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const user = results[0];
        // Compare the inputted password with the hashed password
        const match = await bcrypt.compare(userPassword, user.userPassword);
        if (match) {
            return res.status(200).json({ message: 'Login successful' });
        } else {
            return res.status(401).json({ error: 'Invalid password' });
        }
    });
});

// Get all users
app.get('/users', (req, res) => {
    const sql = "SELECT * FROM users"; // Modify this SQL query as needed
    db.query(sql, (err, results) => {
        if (err) {
            console.error('Error fetching users:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json(results); // Send the user data as JSON
    });
});

app.get('/admins', (req, res) => {
    const sql = "SELECT * FROM admins"; // Modify this SQL query as needed
    db.query(sql, (err, results) => {
        if (err) {
            console.error('Error fetching admins:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json(results); // Send the user data as JSON
    });
});

app.delete('/users/:id', (req, res) => {
    const userId = req.params.id;
    const sql = "DELETE FROM users WHERE userId = ?"; // Replace `id` with the actual column name used for the unique identifier
    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Error deleting user:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json({ message: 'User deleted successfully' });
    });
});

app.delete('/admins/:id', (req, res) => {
    const userId = req.params.id;
    const sql = "DELETE FROM admins WHERE admin_id = ?"; // Replace `id` with the actual column name used for the unique identifier
    db.query(sql, [userId], (err, results) => {
        if (err) {
            console.error('Error deleting admin:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json({ message: 'Admin deleted successfully' });
    });
});

// Start the server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});

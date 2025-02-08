const express = require('express');
const mysql = require('mysql');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcrypt');
const { parse } = require('tldts');

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
            // Return userId along with the message on successful login
            return res.status(200).json({
                message: 'Login successful',
                userId: user.userId // Include userId in the response
            });
        } else {
            return res.status(401).json({ error: 'Invalid password' });
        }
    });
});


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

app.put('/users/:id', (req, res) => {
    const { id } = req.params;
    const updatedUserData = req.body; // This should only contain the fields that are being updated

    const query = `UPDATE users SET ? WHERE userId = ?`; // Adjust the column name as necessary
    db.query(query, [updatedUserData, id], (err, results) => {
        if (err) {
            console.error('Error updating user data:', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        if (results.affectedRows === 0) {
            res.status(404).json({ error: 'User not found' });
            return;
        }
        res.json({ message: 'User data updated successfully' });
    });
});

app.get('/users/:id', (req, res) => {
    const { id } = req.params;
    const query = `SELECT * FROM users WHERE userId = ?`;
    db.query(query, [id], (err, results) => {
      if (err) {
        console.error('Error retrieving user data:', err);
        res.status(500).json({ error: 'Internal server error' });
        return;
      }
      if (results.length === 0) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
      const user = results[0];
      res.json(user);
    });
});
  
app.put('/admins/:id', (req, res) => {
    const { id } = req.params;
    const updatedAdminData = req.body; // This should only contain the fields that are being updated

    const query = `UPDATE admins SET ? WHERE admin_id = ?`; // Adjust the column name as necessary
    db.query(query, [updatedAdminData, id], (err, results) => {
        if (err) {
            console.error('Error updating user data:', err);
            res.status(500).json({ error: 'Internal server error' });
            return;
        }
        if (results.affectedRows === 0) {
            res.status(404).json({ error: 'Admin not found' });
            return;
        }
        res.json({ message: 'Admin data updated successfully' });
    });
});

app.get('/admins/:id', (req, res) => {
    const { id } = req.params;
    const query = `SELECT * FROM admins WHERE admin_id = ?`;
    db.query(query, [id], (err, results) => {
      if (err) {
        console.error('Error retrieving user data:', err);
        res.status(500).json({ error: 'Internal server error' });
        return;
      }
      if (results.length === 0) {
        res.status(404).json({ error: 'Admin not found' });
        return;
      }
      const user = results[0];
      res.json(user);
    });
});

app.post('/submitreport', (req, res) => {
    const { userId, link_reported, report_description } = req.body;

    // Check if userId is provided
    if (!userId || !link_reported) {
        return res.status(400).json({ error: 'userId and link_reported are required' });
    }

    // If report_description is not provided, set it to 'None'
    const description = report_description || 'None';

    // Check if the username in AccountActivity matches the userId
    const getUsernameQuery = "SELECT userUsername FROM users WHERE userId = ?";
    db.query(getUsernameQuery, [userId], (err, results) => {
        if (err) {
            console.error('Error fetching username:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        // If no user found with that userId, return error
        if (results.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }

        const username = results[0].userUsername;

        // Now we can insert the report into the database
        const sql = "INSERT INTO reports (userId, link_reported, report_description) VALUES (?, ?, ?)";
        const values = [userId, link_reported, description];

        db.query(sql, values, (err) => {
            if (err) {
                console.error('Error inserting report:', err);
                return res.status(500).json({ error: 'Internal server error' });
            }

            // Return success message
            res.json({ message: 'Report submitted successfully', username: username });
        });
    });
});

app.get('/reports', (req, res) => {
    const sql = `
        SELECT reports.report_id, reports.link_reported, reports.report_description, 
               users.userUsername 
        FROM reports 
        INNER JOIN users ON reports.userId = users.userId
    `;

    db.query(sql, (err, results) => {
        if (err) {
            console.error('Error fetching reports:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json(results);
    });
});


const extractTLD = (url) => {
    try {
        const hostname = new URL(`https://${url}`).hostname; // Ensure valid URL format
        const parts = hostname.split('.');

        if (parts.length < 2) {
            console.error("Invalid domain:", url);
            return { domain: null, tld: null };
        }

        return {
            url_link: hostname,      // Use url_link instead of domain
            tld: parts[parts.length - 1] // Only the TLD (e.g., "com")
        };
    } catch (error) {
        console.error("Invalid URL:", url);
        return { url_link: null, tld: null };
    }
};

app.post('/reports/approve', (req, res) => {
    const { report_id, link } = req.body;
    const { url_link, tld } = extractTLD(link); 

    if (!url_link || !tld) {
        return res.status(400).json({ error: "Invalid URL" });
    }

    const insertLinkQuery = "INSERT INTO links (url_link, tld) VALUES (?, ?)";
    db.query(insertLinkQuery, [url_link, tld], (err, result) => { 
        if (err) {
            console.error('Error inserting link:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }

        // ✅ Send a response back to the client
        res.status(200).json({ success: true, report_id });
    });
});

app.get('/links', (req, res) => {
    const sql = "SELECT * FROM links"; // Modify this SQL query as needed
    db.query(sql, (err, results) => {
        if (err) {
            console.error('Error fetching links:', err);
            return res.status(500).json({ error: 'Internal server error' });
        }
        res.json(results); // Send the user data as JSON
    });
});



// Start the server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});

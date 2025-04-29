const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const { createClient } = require('@supabase/supabase-js');
const { parse } = require('tldts');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(bodyParser.json()); // Parse JSON request bodies

// Initialize Supabase client - replace with your actual Supabase URL and anon key
const supabaseUrl = 'https://xyhhazbjkgtzcldfdbex.supabase.co';
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5aGhhemJqa2d0emNsZGZkYmV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU5MjU2NjUsImV4cCI6MjA2MTUwMTY2NX0.DvtCkvDVDe-bSXlaaaqMPhalF7FHuwib-1yoTj4DylU';
const supabase = createClient(supabaseUrl, supabaseKey);

// Connect and verify Supabase connection
app.get('/', async (req, res) => {
  try {
    // Simple check to see if we can connect to Supabase
    const { data, error } = await supabase.from('users').select('count');
    if (error) throw error;
    res.json({ message: 'Connected to Supabase', status: 'ok' });
  } catch (err) {
    console.error('Error connecting to Supabase:', err);
    res.status(500).json({ error: 'Failed to connect to database' });
  }
});

app.post('/adminsignup', async (req, res) => {
    const { admin_username, admin_password } = req.body;

    try {
        // Check if the username already exists
        const { data: existingAdmins, error: queryError } = await supabase
            .from('admins')
            .select('admin_id')
            .eq('admin_username', admin_username);

        if (queryError) throw queryError;

        // If a user with the provided username already exists, return an error
        if (existingAdmins && existingAdmins.length > 0) {
            return res.status(400).json({ error: 'Username already exists' });
        }

        // Hash the password before storing it
        const hashedPassword = await bcrypt.hash(admin_password, 10);

        // Insert into Supabase
        const { data, error } = await supabase
            .from('admins')
            .insert([{ admin_username, admin_password: hashedPassword }]);

        if (error) throw error;

        return res.json({ message: 'Registration successful' });
    } catch (err) {
        console.error('Error registering admin:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/adminlogin', async (req, res) => {
    const { admin_username, admin_password } = req.body;

    try {
        // Find the user by username
        const { data: admins, error } = await supabase
            .from('admins')
            .select('*')
            .eq('admin_username', admin_username);

        if (error) throw error;

        // Check if admin exists
        if (!admins || admins.length === 0) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const admin = admins[0];
        // Compare the inputted password with the hashed password
        const match = await bcrypt.compare(admin_password, admin.admin_password);
        if (match) {
            return res.status(200).json({ message: 'Login successful' });
        } else {
            return res.status(401).json({ error: 'Invalid password' });
        }
    } catch (err) {
        console.error('Error logging in:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/usersignup', async (req, res) => {
    const { userusername, userpassword } = req.body;

    try {
        // Check if the username already exists
        const { data: existingUsers, error: queryError } = await supabase
            .from('users')
            .select('userid')
            .eq('userusername', userusername);

        if (queryError) throw queryError;

        if (existingUsers && existingUsers.length > 0) {
            return res.status(400).json({ error: 'Username already exists' });
        }

        const hashedPassword = await bcrypt.hash(userpassword, 10);

        // Insert into Supabase
        const { data, error } = await supabase
            .from('users')
            .insert([{ userusername, userpassword: hashedPassword }]);

        if (error) throw error;

        res.json({ message: 'Registration successful' });
    } catch (err) {
        console.error('Error registering user:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/userlogin', async (req, res) => {
    const { userusername, userpassword } = req.body;

    try {
        // Find the user by username
        const { data: users, error } = await supabase
            .from('users')
            .select('*')
            .eq('userusername', userusername);

        if (error) throw error;

        // Check if user exists
        if (!users || users.length === 0) {
            return res.status(401).json({ error: 'Invalid username or password' });
        }

        const user = users[0];
        // Compare the inputted password with the hashed password
        const match = await bcrypt.compare(userpassword, user.userpassword);
        if (match) {
            // Return userid along with the message on successful login
            return res.status(200).json({
                message: 'Login successful',
                userid: user.userid,
                userusername: user.userusername
            });
        } else {
            return res.status(401).json({ error: 'Invalid password' });
        }
    } catch (err) {
        console.error('Error logging in:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/users', async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('users')
            .select('*');

        if (error) throw error;
        res.json(data);
    } catch (err) {
        console.error('Error fetching users:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/admins', async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('admins')
            .select('*');

        if (error) throw error;
        res.json(data);
    } catch (err) {
        console.error('Error fetching admins:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.delete('/users/:id', async (req, res) => {
    const userid = req.params.id;
    
    try {
        const { data, error } = await supabase
            .from('users')
            .delete()
            .eq('userid', userid);

        if (error) throw error;
        res.json({ message: 'User deleted successfully' });
    } catch (err) {
        console.error('Error deleting user:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.delete('/admins/:id', async (req, res) => {
    const adminId = req.params.id;
    
    try {
        const { data, error } = await supabase
            .from('admins')
            .delete()
            .eq('admin_id', adminId);

        if (error) throw error;
        res.json({ message: 'Admin deleted successfully' });
    } catch (err) {
        console.error('Error deleting admin:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.put('/users/:id', async (req, res) => {
    const { id } = req.params;
    const { newUsername, currentPassword, newPassword } = req.body;

    try {
        // Step 1: Get the current user data
        const { data: users, error: fetchError } = await supabase
            .from('users')
            .select('userusername, userpassword')
            .eq('userid', id);

        if (fetchError) throw fetchError;

        if (!users || users.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }

        const currentUser = users[0];

        // Step 2: If password update is requested, verify and hash new password
        let updatedPassword = currentUser.userpassword;

        if (newPassword) {
            if (!currentPassword) {
                return res.status(400).json({ error: 'Current password is required to change the password' });
            }

            const isMatch = await bcrypt.compare(currentPassword, currentUser.userpassword);
            if (!isMatch) {
                return res.status(401).json({ error: 'Current password is incorrect' });
            }

            updatedPassword = await bcrypt.hash(newPassword, 10);
        }

        // Step 3: Use newUsername if provided, else keep the old one
        const updatedUsername = newUsername || currentUser.userusername;

        // Step 4: Perform the update
        const { data, error: updateError } = await supabase
            .from('users')
            .update({ 
                userusername: updatedUsername, 
                userpassword: updatedPassword 
            })
            .eq('userid', id);

        if (updateError) throw updateError;

        res.json({ message: 'User profile updated successfully' });
    } catch (err) {
        console.error('Error updating user data:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/users/:id', async (req, res) => {
    const { id } = req.params;
    
    try {
        const { data: users, error } = await supabase
            .from('users')
            .select('userid, userusername, userpassword')
            .eq('userid', id);

        if (error) throw error;

        if (!users || users.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }

        const user = users[0];
        res.json({
            userid: user.userid,
            userusername: user.userusername,
            userpassword: user.userpassword
        });
    } catch (err) {
        console.error('Error retrieving user data:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.put('/admins/:id', async (req, res) => {
    const { id } = req.params;
    const updatedAdminData = req.body;
    
    try {
        const { data, error } = await supabase
            .from('admins')
            .update(updatedAdminData)
            .eq('admin_id', id);

        if (error) throw error;
        
        res.json({ message: 'Admin data updated successfully' });
    } catch (err) {
        console.error('Error updating admin data:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/admins/:id', async (req, res) => {
    const { id } = req.params;
    
    try {
        const { data: admins, error } = await supabase
            .from('admins')
            .select('*')
            .eq('admin_id', id);

        if (error) throw error;

        if (!admins || admins.length === 0) {
            return res.status(404).json({ error: 'Admin not found' });
        }

        res.json(admins[0]);
    } catch (err) {
        console.error('Error retrieving admin data:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/submitreport', async (req, res) => {
    const { userid, link_reported, report_description } = req.body;

    // Check if userid is provided
    if (!userid || !link_reported) {
        return res.status(400).json({ error: 'userid and link_reported are required' });
    }

    // If report_description is not provided, set it to 'None'
    const description = report_description || 'None';

    try {
        // Check if the user exists
        const { data: users, error: userError } = await supabase
            .from('users')
            .select('userusername')
            .eq('userid', userid);

        if (userError) throw userError;

        // If no user found with that userid, return error
        if (!users || users.length === 0) {
            return res.status(404).json({ error: 'User not found' });
        }

        const username = users[0].userusername;

        // Insert the report
        const { data, error } = await supabase
            .from('reports')
            .insert([{ 
                userid, 
                link_reported, 
                report_description: description 
            }]);

        if (error) throw error;

        res.json({ message: 'Report submitted successfully', username: username });
    } catch (err) {
        console.error('Error submitting report:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/reports', async (req, res) => {
    try {
        const { data, error } = await supabase
            .from('reports')
            .select(`
                report_id,
                link_reported,
                report_description,
                approved,
                users (userusername)
            `);

        if (error) throw error;

        // Format the response to match the original structure
        const formattedData = data.map(report => ({
            report_id: report.report_id,
            link_reported: report.link_reported,
            report_description: report.report_description,
            approved: report.approved,
            userusername: report.users ? report.users.userusername : null
        }));

        res.json(formattedData);
    } catch (err) {
        console.error('Error fetching reports:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

const extractTLD = (url) => {
    try {
        // Ensure URL is valid and standardized
        const formattedUrl = url.startsWith("http://") || url.startsWith("https://") ? url : `https://${url}`;
        const parsedUrl = new URL(formattedUrl);
        
        const hostname = parsedUrl.hostname;
        const tld = hostname.split('.').pop();

        // Remove trailing slash from pathname
        let cleanedUrl = parsedUrl.href.replace(/\/+$/, '');

        return {
            url_link: cleanedUrl, // Store URL WITHOUT trailing slash
            tld: tld
        };
    } catch (error) {
        console.error("Invalid URL:", url);
        return { url_link: null, tld: null };
    }
};

app.post('/reports/approve', async (req, res) => {
    const { report_id, link } = req.body;
    const { url_link, tld } = extractTLD(link); 

    if (!url_link || !tld) {
        return res.status(400).json({ error: "Invalid URL" });
    }

    try {
        // Get userid from the report before approving
        const { data: reports, error: fetchError } = await supabase
            .from('reports')
            .select('userid')
            .eq('report_id', report_id);

        if (fetchError) throw fetchError;

        if (!reports || reports.length === 0) {
            return res.status(404).json({ error: 'Report not found' });
        }

        const userid = reports[0].userid;

        // Update the report to approved status
        const { error: updateError } = await supabase
            .from('reports')
            .update({ approved: true })
            .eq('report_id', report_id);

        if (updateError) throw updateError;

        // Insert the link with userid
        const { error: insertError } = await supabase
            .from('links')
            .insert([{ url_link, tld, userid: userid }]);

        if (insertError) throw insertError;

        return res.status(200).json({ message: 'Report approved and link stored successfully' });
    } catch (err) {
        console.error('Error in approve process:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/links', async (req, res) => {
    try {
        // This query is more complex in Supabase/PostgreSQL
        // We need to use raw SQL or break it down into multiple operations
        const { data, error } = await supabase.rpc('get_approved_links');

        if (error) {
            // Fallback to a simplified query if the RPC isn't set up
            console.error('RPC error, falling back to basic query:', error);
            
            const { data: basicData, error: basicError } = await supabase
                .from('links')
                .select(`
                    *,
                    users (userusername)
                `);
                
            if (basicError) throw basicError;
            
            // Format the data to match expected structure
            const formattedData = basicData.map(link => ({
                ...link,
                reported_by: link.users ? link.users.userusername : null
            }));
            
            return res.json(formattedData);
        }

        console.log("Fetched links:", data);
        res.json(data);
    } catch (err) {
        console.error('Error fetching links:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.delete('/reports/:report_id', async (req, res) => {
    const reportId = req.params.report_id;
    
    try {
        const { data, error } = await supabase
            .from('reports')
            .delete()
            .eq('report_id', reportId);

        if (error) throw error;
        
        res.json({ message: 'Report deleted successfully' });
    } catch (err) {
        console.error('Error deleting report:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/links/:userid', async (req, res) => {
    const { userid } = req.params;

    try {
        // This query is complex and might need a custom PostgreSQL function in Supabase
        const { data, error } = await supabase.rpc('get_user_links', { userid: userid });

        if (error) {
            // Fallback to a simpler query if the RPC function isn't set up
            console.error('RPC error, falling back to basic query:', error);
            
            const { data: basicData, error: basicError } = await supabase
                .from('links')
                .select('link_id, url_link, tld, date_verified')
                .eq('userid', userid);
                
            if (basicError) throw basicError;
            return res.json(basicData);
        }

        console.log("Fetched user-specific links:", data);
        res.json(data);
    } catch (err) {
        console.error('Error fetching links for user:', err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

// Start the server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
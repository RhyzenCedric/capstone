import React, { useState, useEffect } from 'react';
import axios from 'axios'; // Import Axios
import "../../css/EditAdminDetails.css";

const EditAdminDetails = ({ admin_id, onUpdate, onClose }) => {
    const [admin_username, setAdmin_username] = useState('');

    useEffect(() => {
        const fetchAdmin = async () => {
            try {
                const response = await axios.get(`http://localhost:5000/admins/${admin_id}`); // Fetch the specific admin by ID
                setAdmin_username(response.data.admin_username); // Set the fetched admin's username
            } catch (error) {
                console.error('Error fetching admin:', error);
            }
        };

        if (admin_id) {
            fetchAdmin(); // Fetch admin data when admin_id is available
        }
    }, [admin_id]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const updatedData = {
            admin_username,
        };

        try {
            const response = await axios.put(`http://localhost:5000/admins/${admin_id}`, updatedData); // Use admin_id for updating

            if (response.status === 200) {
                onUpdate(); 
                onClose();  
            } else {
                throw new Error('Failed to update admin');
            }
        } catch (error) {
            console.error('Error updating admin:', error);
        }
    };

    return (
        <div className="edit-admin-modal">
            <h2>Edit Admin</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>
                        Username:
                        <input
                            type="text"
                            value={admin_username} // Pre-fill with fetched admin_username
                            onChange={(e) => setAdmin_username(e.target.value)} // Update state on input change
                            required
                        />
                    </label>
                </div>
                <button type="submit">Update</button>
                <button type="button" onClick={onClose}>Cancel</button>
            </form>
        </div>
    );
};

export default EditAdminDetails;

import React, { useState, useEffect } from 'react';

const EditUserDetailsAdmin = ({ userId, username, onUpdate, onClose }) => {
    const [user, setUser] = useState(null); 
    const [userUsername, setUserUsername] = useState('');
    const [userEmail, setUserEmail] = useState('');

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await fetch(`http://localhost:5000/users/${userId}`); // Use userId for fetching
                if (!response.ok) throw new Error('Failed to fetch user data');
                const userData = await response.json();
                setUser(userData);
                setUserUsername(userData.userUsername); 
                setUserEmail(userData.userEmail);
            } catch (error) {
                console.error('Error fetching user:', error);
            }
        };

        fetchUser();
    }, [userId]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const updatedData = {
            userUsername,
            userEmail,
        };

        try {
            const response = await fetch(`http://localhost:5000/users/${userId}`, { // Use userId for updating
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatedData),
            });

            if (!response.ok) throw new Error('Failed to update user');
            onUpdate(); 
            onClose();  
        } catch (error) {
            console.error('Error updating user:', error);
        }
    };

    if (!user) {
        return <div>Loading...</div>; 
    }

    return (
        <div className="edit-user-modal">
            <h2>Edit User</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>
                        Username:
                        <input
                            type="text"
                            value={userUsername} 
                            onChange={(e) => setUserUsername(e.target.value)}
                            placeholder="New Username"
                            required
                        />
                    </label>
                </div>
                <div>
                    <label>
                        Email:
                        <input
                            type="email"
                            value={userEmail} 
                            onChange={(e) => setUserEmail(e.target.value)}
                            placeholder="New Email"
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

export default EditUserDetailsAdmin;

import React, { useEffect, useState } from 'react';
import TopNav from "../NavBars/TopNav";
import "../../css/Users.css"

const Users = () => {
    const [users, setUsers] = useState([]); // State to hold user data
    const [loading, setLoading] = useState(true); // State to manage loading status

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await fetch('http://localhost:5000/users'); // Adjust the URL based on your setup
                if (!response.ok) throw new Error('Failed to fetch users');
                const data = await response.json();
                setUsers(data); // Set the fetched data to the state
            } catch (error) {
                console.error('Error fetching users:', error);
            } finally {
                setLoading(false); // Set loading to false after fetching
            }
        };

        fetchUsers();
    }, []); // Run this effect only once on component mount

    const handleEdit = (userId) => {
        // Implement your edit logic here
        console.log('Edit user with ID:', userId);
        // For example, redirect to an edit form or open a modal
    };

    const handleDelete = async (userId) => {
        if (window.confirm("Are you sure you want to delete this user?")) {
            try {
                const response = await fetch(`http://localhost:5000/users/${userId}`, {
                    method: 'DELETE',
                });
                if (!response.ok) throw new Error('Failed to delete user');
                // Filter out the deleted user from the state
                setUsers((prevUsers) => prevUsers.filter(user => user.userId !== userId)); // Use userId from your backend
            } catch (error) {
                console.error('Error deleting user:', error);
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>; // Show loading state
    }

    return (
        <>
            <TopNav />
            <h1 class='user-header'>Users</h1>
            <table>
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Actions</th> {/* New column for buttons */}
                    </tr>
                </thead>
                <tbody>
                    {users.map((user) => (
                        <tr key={user.userId}> {/* Use userId as the unique identifier */}
                            <td>{user.userUsername}</td>
                            <td>{user.userEmail}</td>
                            <td>
                                <button onClick={() => handleEdit(user.userId)}>Edit</button>
                                <button onClick={() => handleDelete(user.userId)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
};

export default Users;

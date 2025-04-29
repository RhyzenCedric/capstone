import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
import axios from 'axios'; // Import Axios
import TopNav from "../NavBars/TopNav";
import EditUserDetailsAdmin from '../EditDetails/EditUserDetailsAdmin'; 
import Modal from '../EditDetails/EditUserDetailsAdminModal';
import "../../css/Users.css";

const Users = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingUser, setEditingUser] = useState(null); 
    const navigate = useNavigate(); 

    useEffect(() => {
        fetchUsers(); // Fetch users on component mount
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await axios.get('http://localhost:5000/users'); // Use Axios to fetch users
            setUsers(response.data); // Set users data
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setLoading(false); // Set loading to false after fetching
        }
    };

    const handleEdit = (user) => {
        setEditingUser(user); // Set the user to edit
    };

    const handleDelete = async (userid) => {
        if (window.confirm("Are you sure you want to delete this user?")) {
            try {
                const response = await axios.delete(`http://localhost:5000/users/${userid}`); // Use Axios to delete user
                if (response.status === 200) { // Check if deletion was successful
                    setUsers((prevUsers) => prevUsers.filter(user => user.userid !== userid)); // Update users state
                } else {
                    throw new Error('Failed to delete user');
                }
            } catch (error) {
                console.error('Error deleting user:', error); // Log the error
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>; // Show loading message
    }

    return (
        <>
            <TopNav />
            <h1 className='user-header'>Users</h1>
            <table>
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((user) => (
                        <tr key={user.userid}>
                            <td>{user.userusername}</td>
                            <td>
                                <button className="user-edit-button" onClick={() => handleEdit(user)}>Edit</button>
                                <button className="user-delete-button" onClick={() => handleDelete(user.userid)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {editingUser && ( 
                <Modal onClose={() => setEditingUser(null)}>
                    <EditUserDetailsAdmin
                        userId={editingUser.userid}
                        username={editingUser.userusername}
                        onUpdate={fetchUsers} // Pass fetchUsers to update after the modal
                        onClose={() => setEditingUser(null)} // Close modal
                    />
                </Modal>
            )}
        </>
    );
};

export default Users;

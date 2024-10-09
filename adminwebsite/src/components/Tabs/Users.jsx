import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
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
            const response = await fetch('http://localhost:5000/users');
            if (!response.ok) throw new Error('Failed to fetch users');
            const data = await response.json();
            setUsers(data);
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (user) => {
        setEditingUser(user);
    };

    const handleDelete = async (userId) => {
        if (window.confirm("Are you sure you want to delete this user?")) {
            try {
                const response = await fetch(`http://localhost:5000/users/${userId}`, {
                    method: 'DELETE',
                });
                if (!response.ok) throw new Error('Failed to delete user');
                setUsers((prevUsers) => prevUsers.filter(user => user.userId !== userId));
            } catch (error) {
                console.error('Error deleting user:', error);
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>;
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
                        <tr key={user.userId}>
                            <td>{user.userUsername}</td>
                            <td>{user.userEmail}</td>
                            <td>
                                <button className="user-edit-button" onClick={() => handleEdit(user)}>Edit</button>
                                <button className="user-delete-button" onClick={() => handleDelete(user.userId)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {editingUser && ( 
                <Modal onClose={() => setEditingUser(null)}>
                    <EditUserDetailsAdmin
                        userId={editingUser.userId}
                        username={editingUser.userUsername}
                        onUpdate={fetchUsers} // Pass fetchUsers to update after the modal
                        onClose={() => setEditingUser(null)} // Close modal
                    />
                </Modal>
            )}
        </>
    );
};

export default Users;

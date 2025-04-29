import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../../supabaseClient'; // ✅ Import Supabase
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
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        setLoading(true);
        const { data, error } = await supabase.from('users').select('*');
        if (error) {
            console.error('Error fetching users:', error.message);
        } else {
            setUsers(data);
        }
        setLoading(false);
    };

    const handleEdit = (user) => {
        setEditingUser(user);
    };

    const handleDelete = async (userid) => {
        if (window.confirm("Are you sure you want to delete this user?")) {
            const { error } = await supabase.from('users').delete().eq('userid', userid);
            if (error) {
                console.error('Error deleting user:', error.message);
            } else {
                setUsers((prevUsers) => prevUsers.filter(user => user.userid !== userid));
            }
        }
    };

    if (loading) return <h1>Loading...</h1>;

    return (
        <>
            <TopNav />
            <h1 className='user-header'>Users</h1>
            <table>
                <thead>
                    <tr>
                        <th>Username</th>
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
                        onUpdate={fetchUsers}
                        onClose={() => setEditingUser(null)}
                    />
                </Modal>
            )}
        </>
    );
};

export default Users;

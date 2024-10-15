import { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom'; 
import TopNav from "../NavBars/TopNav";
import EditAdminDetails from '../EditDetails/EditAdminDetails'; 
import Modal from '../EditDetails/EditAdminDetailsModal';
import axios from 'axios'; // Import Axios
import "../../css/Admins.css"

const Admins = () => {
    const [admins, setAdmins] = useState([]); // State to hold user data
    const [loading, setLoading] = useState(true); // State to manage loading status
    const [editingAdmin, setEditingAdmin] = useState(null); 
    const navigate = useNavigate(); 
    
    useEffect(() => {
        fetchAdmins();
    }, []); // Run this effect only once on component mount
    
    const fetchAdmins = async () => {
        try {
            const response = await axios.get('http://localhost:5000/admins'); // Use Axios to fetch admins
            setAdmins(response.data); // Set the fetched data to the state
        } catch (error) {
            console.error('Error fetching admins:', error);
        } finally {
            setLoading(false); // Set loading to false after fetching
        }
    };

    const handleEdit = (admin) => {
        setEditingAdmin(admin);
    };

    const handleDelete = async (admin_id) => {
        if (window.confirm("Are you sure you want to delete this admin?")) {
            try {
                const response = await axios.delete(`http://localhost:5000/admins/${admin_id}`); // Use Axios to delete admin
                if (response.status === 200) {
                    setAdmins((prevAdmins) => prevAdmins.filter(admin => admin.admin_id !== admin_id)); // Update state after deletion
                } else {
                    throw new Error('Failed to delete admin');
                }
            } catch (error) {
                console.error('Error deleting admin:', error);
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>; // Show loading state
    }
    
    return (
        <>
            <TopNav />
            <h1 className="admin-header">Admins</h1>
            <table>
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {admins.map((admin) => (
                        <tr key={admin.admin_id}>
                            <td>{admin.admin_username}</td>
                            <td>
                                <button className="admin-edit-button" onClick={() => handleEdit(admin)}>Edit</button>
                                <button className="admin-delete-button" onClick={() => handleDelete(admin.admin_id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {editingAdmin && ( 
                <Modal onClose={() => setEditingAdmin(null)}>
                    <EditAdminDetails
                        admin_id={editingAdmin.admin_id}
                        admin_username={editingAdmin.admin_username}
                        onUpdate={fetchAdmins} // Pass fetchAdmins to update after the modal
                        onClose={() => setEditingAdmin(null)} // Close modal
                    />
                </Modal>
            )}
        </>
    );
};

export default Admins;

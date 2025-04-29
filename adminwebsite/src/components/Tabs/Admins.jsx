import { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom'; 
import TopNav from "../NavBars/TopNav";
import EditAdminDetails from '../EditDetails/EditAdminDetails'; 
import Modal from '../EditDetails/EditAdminDetailsModal';
import { supabase } from '../../supabaseClient'; // ✅ import Supabase client
import "../../css/Admins.css"

const Admins = () => {
    const [admins, setAdmins] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingAdmin, setEditingAdmin] = useState(null); 
    const navigate = useNavigate();

    useEffect(() => {
        fetchAdmins();
    }, []);

    const fetchAdmins = async () => {
        setLoading(true);
        const { data, error } = await supabase
            .from('admins')
            .select('*');
        if (error) {
            console.error('Error fetching admins:', error.message);
        } else {
            setAdmins(data);
        }
        setLoading(false);
    };

    const handleEdit = (admin) => {
        setEditingAdmin(admin);
    };

    const handleDelete = async (admin_id) => {
        if (window.confirm("Are you sure you want to delete this admin?")) {
            const { error } = await supabase
                .from('admins')
                .delete()
                .eq('admin_id', admin_id);
            if (error) {
                console.error('Error deleting admin:', error.message);
            } else {
                setAdmins((prev) => prev.filter(admin => admin.admin_id !== admin_id));
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>;
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
                        onUpdate={fetchAdmins}
                        onClose={() => setEditingAdmin(null)}
                    />
                </Modal>
            )}
        </>
    );
};

export default Admins;

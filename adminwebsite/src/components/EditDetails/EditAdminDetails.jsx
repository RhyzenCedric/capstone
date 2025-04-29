import React, { useState, useEffect } from 'react';
import { supabase } from '../../supabaseClient'; // ✅ Import Supabase client
import "../../css/EditAdminDetails.css";

const EditAdminDetails = ({ admin_id, onUpdate, onClose }) => {
    const [admin_username, setAdmin_username] = useState('');

    useEffect(() => {
        const fetchAdmin = async () => {
            const { data, error } = await supabase
                .from('admins')
                .select('admin_username')
                .eq('admin_id', admin_id)
                .single();

            if (error) {
                console.error('Error fetching admin:', error.message);
            } else {
                setAdmin_username(data.admin_username);
            }
        };

        if (admin_id) {
            fetchAdmin();
        }
    }, [admin_id]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const { error } = await supabase
            .from('admins')
            .update({ admin_username })
            .eq('admin_id', admin_id);

        if (error) {
            console.error('Error updating admin:', error.message);
        } else {
            onUpdate();  // Refresh the list in parent
            onClose();   // Close modal
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
                            value={admin_username}
                            onChange={(e) => setAdmin_username(e.target.value)}
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

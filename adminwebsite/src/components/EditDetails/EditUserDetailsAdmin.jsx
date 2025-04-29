import React, { useState, useEffect } from 'react';
import { supabase } from '../../supabaseClient'; // ✅ Import Supabase
import "../../css/EditUserDetailsAdmin.css";

const EditUserDetailsAdmin = ({ userId, onUpdate, onClose }) => {
    const [userUsername, setUserUsername] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUser = async () => {
            const { data, error } = await supabase
                .from('users')
                .select('userusername')
                .eq('userid', userId)
                .single();

            if (error) {
                console.error('Error fetching user:', error.message);
            } else {
                setUserUsername(data.userusername);
            }
            setLoading(false);
        };

        fetchUser();
    }, [userId]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const { error } = await supabase
            .from('users')
            .update({
                userusername: userUsername,
            })
            .eq('userid', userId);

        if (error) {
            console.error('Error updating user:', error.message);
        } else {
            onUpdate();
            onClose();
        }
    };

    if (loading) return <div>Loading...</div>;

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

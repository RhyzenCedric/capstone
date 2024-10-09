import { useEffect, useState } from "react";
import TopNav from "../NavBars/TopNav";

const Admins = () => {
    const [admins, setAdmins] = useState([]); // State to hold user data
    const [loading, setLoading] = useState(true); // State to manage loading status
    
    useEffect(() => {
        const fetchAdmins = async () => {
            try {
                const response = await fetch('http://localhost:5000/admins'); // Adjust the URL based on your setup
                const data = await response.json();
                setAdmins(data); // Set the fetched data to the state
            } catch (error) {
                console.error('Error fetching admins:', error);
            } finally {
                setLoading(false); // Set loading to false after fetching
            }
        };
    
        fetchAdmins();
    }, []); // Run this effect only once on component mount
    
    // const handleEdit = (userId) => {
    //     // Implement your edit logic here
    //     console.log('Edit user with ID:', userId);
    //     // For example, redirect to an edit form or open a modal
    // };

    const handleDelete = async (admin_id) => {
        if (window.confirm("Are you sure you want to delete this admin?")) {
            try {
                const response = await fetch(`http://localhost:5000/admins/${admin_id}`, {
                    method: 'DELETE',
                });
                if (!response.ok) throw new Error('Failed to delete admin');
                // Filter out the deleted user from the state
                setAdmins((prevAdmins) => prevAdmins.filter(admin => admin.admin_id !== admin_id)); // Use userId from your backend
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
            <h1>Admins</h1>
            <table>
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Actions</th>
                        {/* Add more headers based on your user data structure */}
                    </tr>
                </thead>
                <tbody>
                    {admins.map((admin) => (
                        <tr key={admin.admin_id}> {/* Assuming `id` is the unique identifier */}
                            <td>{admin.admin_username}</td>
                            <td>
                                <button onClick={() => handleEdit(admin.admin_id)}>Edit</button>
                                <button onClick={() => handleDelete(admin.admin_id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
};

export default Admins;

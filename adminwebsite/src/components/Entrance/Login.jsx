import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../../supabaseClient'; // Import Supabase
import '../../css/Login.css';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [passwordVisible, setPasswordVisible] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const { data: admins, error } = await supabase
                .from('admins')
                .select('*')
                .eq('admin_username', username);

            if (error || !admins.length) {
                alert('Invalid username or password');
                return;
            }

            const admin = admins[0];
            if (admin.admin_password === password) { // Replace with hashed password comparison in production
                console.log("Logged In Successfully");
                navigate('/dashboard'); // Navigate to dashboard on successful login
            } else {
                alert('Invalid password');
            }
        } catch (error) {
            console.error('Error during login:', error);
            alert('An error occurred during login. Please try again.');
        }
    };


    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible); // Toggle password visibility
    };

    const handleSignUp = () => {
        navigate('/signup'); // Redirect to the signup page
    };

    return (
        <div className="login-container">
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Username:</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div className="password">
                    <label>Password:</label>
                    <input
                        type={passwordVisible ? 'text' : 'password'}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <i
                        className={`fas ${passwordVisible ? 'fa-eye-slash' : 'fa-eye'}`}
                        onClick={togglePasswordVisibility}
                        aria-hidden="true"
                    ></i>
                </div>
                <button type="submit">Login</button>
            </form>
            {/* Sign Up Button */}
            <button onClick={handleSignUp} className="sign-up-button">
                Sign Up
            </button>
        </div>
    );
};

export default Login;

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // Import Axios
import '../../css/Login.css';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [passwordVisible, setPasswordVisible] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post('http://localhost:5000/adminlogin', {
                admin_username: username,
                admin_password: password,
            });

            if (response.status === 200) {
                console.log("Logged In Successfully");
                navigate('/dashboard'); // Navigate to dashboard on successful login
            } else {
                alert(response.data.message); // Show error message if login fails
            }
        } catch (error) {
            console.error('Error during login:', error);
            alert('An error occurred during login. Please try again.');
        }
    };

    const handleGuestLogin = () => {
        navigate('/dashboard'); // Direct guest login to dashboard
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
            <button onClick={handleGuestLogin} className="guest-button">
                Continue as Guest
            </button>
        </div>
    );
};

export default Login;

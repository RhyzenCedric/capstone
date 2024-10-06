import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../css/Login.css'; 

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        // Here, add logic to handle user authentication (e.g., API call)
        console.log('Logging in with:', { email, password });
        // Redirect to the dashboard upon successful login
        navigate('/dashboard'); // Redirect to users as an example
    };

    const handleGuestLogin = () => {
        // Redirect to the dashboard without authentication
        navigate('/dashboard'); // Redirect to users as an example
    };

    return (
        <div className="login-container"> {/* Add class name for styling */}
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Email:</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Password:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Login</button>
            </form>
            <button onClick={handleGuestLogin} className="guest-button">
                Continue as Guest
            </button>
        </div>
    );
};

export default Login;

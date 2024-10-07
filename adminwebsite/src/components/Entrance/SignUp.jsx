import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../css/Signup.css';

const Signup = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordVisible, setPasswordVisible] = useState(false);
    const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);
    const [errorMessage, setErrorMessage] = useState(''); // State for error message
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (password !== confirmPassword) {
            setErrorMessage('Passwords do not match');
            return;
        }

        try {
            const response = await fetch('http://localhost:5000/adminsignup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    admin_username: username,
                    admin_password: password,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                console.log('Registered Succesfully');
                navigate('/'); // Redirect on successful signup
            } else {
                // Handle error response from the backend
                setErrorMessage(data.error);
            }
        } catch (error) {
            console.error('Error during signup:', error);
            setErrorMessage('An error occurred while signing up');
        }
    };

    const togglePasswordVisibility = () => {
        setPasswordVisible(!passwordVisible);
    };

    const toggleConfirmPasswordVisibility = () => {
        setConfirmPasswordVisible(!confirmPasswordVisible);
    };

    return (
        <div className="signup-container"> {/* Add this wrapper */}
            <h2>Signup</h2>
            {errorMessage && <div className="error-message">{errorMessage}</div>} {/* Display error message */}
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
                <div className="password">
                    <label>Confirm Password:</label>
                    <input
                        type={confirmPasswordVisible ? 'text' : 'password'}
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                    />
                    <i 
                        className={`fas ${confirmPasswordVisible ? 'fa-eye-slash' : 'fa-eye'}`} 
                        onClick={toggleConfirmPasswordVisibility}
                        aria-hidden="true"
                    ></i>
                </div>
                <button type="submit">Signup</button>
            </form>
        </div>
    );
};

export default Signup;

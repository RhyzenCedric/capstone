import { BrowserRouter as Router, Routes, Route, Link, BrowserRouter } from 'react-router-dom';
import Users from './components/Tabs/Users';
import Admins from './components/Tabs/Admins';
import Links from './components/Tabs/Links';
import Reports from './components/Tabs/Reports';
import Settings from './components/Tabs/Settings';
import Login from './components/Entrance/Login';
import Signup from './components/Entrance/SignUp';
import DashboardMainScreen from './components/DashboardMainScreen'; 
import EditUserDetailsAdmin from './components/EditDetails/EditUserDetailsAdmin';

const App = () => {
    return (
            <Router>
                <Routes>
                    <Route path="/" element={<Login />} />
                    <Route path="/dashboard" element={<DashboardMainScreen />} />

                    <Route path="/dashboard/users" element={<Users />} />
                    <Route path="/dashboard/users/edit/:username" element={<EditUserDetailsAdmin />} />

                    <Route path="/dashboard/admins" element={<Admins />} />
                    <Route path="/dashboard/links" element={<Links />} />
                    <Route path="/dashboard/reports" element={<Reports />} />
                    <Route path="/dashboard/settings" element={<Settings />} />
                    
                    <Route path="/signup" element={<Signup />} />
                </Routes>
            </Router>
    );
};

export default App;

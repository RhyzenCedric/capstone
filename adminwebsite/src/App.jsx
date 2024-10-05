import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Users from './components/Tabs/Users';
import Admins from './components/Tabs/Admins';
import Links from './components/Tabs/Links';
import Reports from './components/Tabs/Reports';
import Settings from './components/Tabs/Settings';
import './index.css';

const App = () => {
    return (
        <Router>
            <div className="dashboard">
                <nav>
                    <ul className="tab-list">
                        <li><Link to="/users">Users</Link></li>
                        <li><Link to="/admins">Admins</Link></li>
                        <li><Link to="/links">Links</Link></li>
                        <li><Link to="/reports">Reports</Link></li>
                        <li><Link to="/settings">Settings</Link></li>
                    </ul>
                </nav>
                <div className="content">
                    <Routes>
                        <Route path="/users" element={<Users />} />
                        <Route path="/admins" element={<Admins />} />
                        <Route path="/links" element={<Links />} />
                        <Route path="/reports" element={<Reports />} />
                        <Route path="/settings" element={<Settings />} />
                    </Routes>
                </div>
            </div>
        </Router>
    );
};

export default App;

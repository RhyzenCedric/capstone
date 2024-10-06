import { Link } from "react-router-dom";
import '../../css/TopNav.css';


const TopNav = () => {
    return (
        <div className="TopNav">
            <nav>
                <h1>Dashboard</h1>
                <ul className="tab-list">
                    <li><Link to="/dashboard/users">Users</Link></li>
                    <li><Link to="/dashboard/admins">Admins</Link></li>
                    <li><Link to="/dashboard/links">Links</Link></li>
                    <li><Link to="/dashboard/reports">Reports</Link></li>
                    <li><Link to="/dashboard/settings">Settings</Link></li>
                </ul>
            </nav>
        </div>
    );
};

export default TopNav;
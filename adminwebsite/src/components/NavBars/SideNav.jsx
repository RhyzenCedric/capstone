import React from "react";
import { Link } from "react-router-dom";
import '../../css/SideNav.css';

const SideNav = ({ toggleSidebar }) => {
    return (
        <div className="SideNav">
            <div className="header-container">
                <h1>Dashboard</h1>
                <button className="close-btn" onClick={toggleSidebar}>
                    <i className="fas fa-times"></i> {/* Font Awesome icon for close */}
                </button>
            </div>
            <nav>
                <ul className="side-tab-list">
                    <li><Link to="/dashboard/users"><i className="fas fa-users"/> Users</Link></li>
                    <li><Link to="/dashboard/admins"><i className="fas fa-user-shield"/> Admins</Link></li>
                    <li><Link to="/dashboard/links"><i className="fas fa-link"/> Links</Link></li>
                    <li><Link to="/dashboard/reports"><i className="fas fa-file-alt"/> Reports</Link></li>
                    <li><Link to="/dashboard/settings"><i className="fas fa-cog"/> Settings</Link></li>
                </ul>
            </nav>
        </div>
    );
};

export default SideNav;

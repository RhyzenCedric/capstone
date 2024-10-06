import React, { useState } from "react";
import { Link } from "react-router-dom";
import '../../css/TopNav.css';
import SideNav from "./SideNav";

const TopNav = () => {
    // State to control sidebar visibility
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    // Function to toggle the sidebar
    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return (
        <div className="TopNav">
            <button className="sidebar-toggle" onClick={toggleSidebar}>
                <i className="fas fa-bars"></i>
            </button>

            <h1>Dashboard</h1>
            <nav>
                <ul className="tab-list">
                    <li><Link to="/dashboard/users"><i className="fas fa-users"/> Users</Link></li>
                    <li><Link to="/dashboard/admins"><i className="fas fa-user-shield"/> Admins</Link></li>
                    <li><Link to="/dashboard/links"><i className="fas fa-link"/> Links</Link></li>
                    <li><Link to="/dashboard/reports"><i className="fas fa-file-alt"/> Reports</Link></li>
                    <li><Link to="/dashboard/settings"><i className="fas fa-cog"/> Settings</Link></li>
                </ul>
            </nav>
            {/* Conditionally render the Sidebar with the toggle function passed as a prop */}
            {isSidebarOpen && <SideNav toggleSidebar={toggleSidebar} />}
        </div>
    );
};

export default TopNav;

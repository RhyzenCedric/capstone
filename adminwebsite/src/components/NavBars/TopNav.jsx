import React, { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import '../../css/TopNav.css';
import SideNav from "./SideNav";

const TopNav = () => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);
    const location = useLocation(); // Get the current location

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
                    <li>
                        <Link to="/dashboard/users" className={location.pathname === "/dashboard/users" ? "active" : ""}>
                            <i className="fas fa-users"/> Users
                        </Link>
                    </li>
                    <li>
                        <Link to="/dashboard/admins" className={location.pathname === "/dashboard/admins" ? "active" : ""}>
                            <i className="fas fa-user-shield"/> Admins
                        </Link>
                    </li>
                    <li>
                        <Link to="/dashboard/links" className={location.pathname === "/dashboard/links" ? "active" : ""}>
                            <i className="fas fa-link"/> Links
                        </Link>
                    </li>
                    <li>
                        <Link to="/dashboard/reports" className={location.pathname === "/dashboard/reports" ? "active" : ""}>
                            <i className="fas fa-file-alt"/> Reports
                        </Link>
                    </li>
                    <li>
                        <Link to="/dashboard/settings" className={location.pathname === "/dashboard/settings" ? "active" : ""}>
                            <i className="fas fa-cog"/> Settings
                        </Link>
                    </li>
                </ul>
            </nav>
            {isSidebarOpen && <SideNav toggleSidebar={toggleSidebar} />}
        </div>
    );
};

export default TopNav;

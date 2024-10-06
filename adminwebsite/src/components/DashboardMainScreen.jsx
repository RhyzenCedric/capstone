import React, { useState } from 'react';
import SideNav from './NavBars/SideNav';
import TopNav from './NavBars/TopNav';
import '../css/DashboardMainScreen.css';

const DashboardMainScreen = () => {
    const [isSidebarVisible, setIsSidebarVisible] = useState(false); // State to manage sidebar visibility

    const toggleSidebar = () => {
        setIsSidebarVisible((prev) => !prev); // Toggle sidebar visibility
    };

    return (
        <div className="dashboard-container">
            <TopNav toggleSidebar={toggleSidebar} /> {/* Pass toggle function to TopNav */}
            {isSidebarVisible && <SideNav />} {/* Conditionally render SideNav */}
            <div className={`content ${isSidebarVisible ? 'with-sidebar' : ''}`}></div> {/* Add class based on visibility */}
        </div>
    );
};

export default DashboardMainScreen;

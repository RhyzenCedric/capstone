// src/Dashboard.js
import React, { useState, useEffect, useRef } from 'react';
import Users from './Users';
import Admins from './Admins';
import Reports from './Reports';
import Links from './Links';
import Settings from './Settings';

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('users');
  const [isSidebarOpen, setSidebarOpen] = useState(false);
  const sidebarRef = useRef(null); // Ref for sidebar
  const menuButtonRef = useRef(null); // Ref for menu button

  // Close sidebar when clicking outside of it
  useEffect(() => {
    const handleOutsideClick = (event) => {
      // Close sidebar only if the click is outside both sidebar and the menu button
      if (
        sidebarRef.current &&
        !sidebarRef.current.contains(event.target) &&
        menuButtonRef.current &&
        !menuButtonRef.current.contains(event.target)
      ) {
        setSidebarOpen(false);
      }
    };

    if (isSidebarOpen) {
      document.addEventListener('click', handleOutsideClick);
    } else {
      document.removeEventListener('click', handleOutsideClick);
    }

    return () => {
      document.removeEventListener('click', handleOutsideClick);
    };
  }, [isSidebarOpen]);

  return (
    <div className="relative flex h-screen bg-[#E9EEF2]">
      {/* Toggle Sidebar Button (Left side of Admin Dashboard header) */}
      {!isSidebarOpen && ( // Hide the button if the sidebar is open
        <button
          ref={menuButtonRef} // Attach ref to the menu button
          className="absolute top-4 left-4 z-10 bg-[#326789] text-white p-2 rounded-md"
          onClick={(e) => {
            e.stopPropagation(); // Prevent event from bubbling up to the document click listener
            setSidebarOpen(true);
          }}
        >
          ☰ {/* Menu icon */}
        </button>
      )}

      {/* Sidebar */} 
      <aside
        ref={sidebarRef} // Attach ref to the sidebar
        className={`fixed top-0 left-0 h-full w-64 bg-[#326789] text-white p-4 pt-4 transition-transform transform ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex items-center justify-between mb-2"> {/* Reduced margin-bottom */}
          <h2 className="text-2xl font-bold">Admin Panel</h2>
          {/* Close Sidebar Button */}
          <button
            className="bg-[#E9EEF2] text-[#326789] p-1 rounded-md" // Reduced padding
            onClick={() => setSidebarOpen(false)}
          >
            ❌ {/* Close icon */}
          </button>
        </div>

        <nav>
          <ul className="space-y-1"> {/* Reduced space between items */}
            <li>
              <button
                className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                onClick={() => {
                  setActiveTab('users');
                  setSidebarOpen(false);
                }}
              >
                <i className="fas fa-users mr-2"></i> Users
              </button>
            </li>
            <li>
              <button
                className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                onClick={() => {
                  setActiveTab('admins');
                  setSidebarOpen(false);
                }}
              >
                <i className="fas fa-user-shield mr-2"></i> Admins
              </button>
            </li>
            <li>
              <button
                className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                onClick={() => {
                  setActiveTab('reports');
                  setSidebarOpen(false);
                }}
              >
                <i className="fas fa-file-alt mr-2"></i> Reports
              </button>
            </li>
            <li>
              <button
                className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                onClick={() => {
                  setActiveTab('links');
                  setSidebarOpen(false);
                }}
              >
                <i className="fas fa-link mr-2"></i> Links
              </button>
            </li>
            <li>
              <button
                className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                onClick={() => {
                  setActiveTab('settings');
                  setSidebarOpen(false);
                }}
              >
                <i className="fas fa-cog mr-2"></i> Settings
              </button>
            </li>
          </ul>
        </nav>
      </aside>


      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Navbar with Tab Navigation */}
        <header className="bg-white shadow p-4 w-full pl-16">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold">Admin Dashboard</h1>
            <div className="flex space-x-4">
              <button
                className={`px-4 py-2 rounded-md flex items-center ${
                  activeTab === 'users' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
                }`}
                onClick={() => setActiveTab('users')}
              >
                <i className="fas fa-users mr-2"></i> Users
              </button>
              <button
                className={`px-4 py-2 rounded-md flex items-center ${
                  activeTab === 'admins' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
                }`}
                onClick={() => setActiveTab('admins')}
              >
                <i className="fas fa-user-shield mr-2"></i> Admins
              </button>
              <button
                className={`px-4 py-2 rounded-md flex items-center ${
                  activeTab === 'reports' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
                }`}
                onClick={() => setActiveTab('reports')}
              >
                <i className="fas fa-file-alt mr-2"></i> Reports
              </button>
              <button
                className={`px-4 py-2 rounded-md flex items-center ${
                  activeTab === 'links' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
                }`}
                onClick={() => setActiveTab('links')}
              >
                <i className="fas fa-link mr-2"></i> Links
              </button>
              <button
                className={`px-4 py-2 rounded-md flex items-center ${
                  activeTab === 'settings' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
                }`}
                onClick={() => setActiveTab('settings')}
              >
                <i className="fas fa-cog mr-2"></i> Settings
              </button>
            </div>
          </div>
        </header>

        {/* Tab Content */}
        <div className="flex-1 p-4 overflow-auto">
          {activeTab === 'users' && <Users />}
          {activeTab === 'admins' && <Admins />}
          {activeTab === 'reports' && <Reports />}
          {activeTab === 'links' && <Links />}
          {activeTab === 'settings' && <Settings />}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

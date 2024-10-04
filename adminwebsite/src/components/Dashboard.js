import React, { useState, useEffect, useRef } from 'react';
import Users from './Users';
import Admins from './Admins';
import Reports from './Reports';
import Links from './Links';
import Settings from './Settings';

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('users');
  const [isSidebarOpen, setSidebarOpen] = useState(false);
  const sidebarRef = useRef(null);
  const menuButtonRef = useRef(null);

  useEffect(() => {
    const handleOutsideClick = (event) => {
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
      {/* Toggle Sidebar Button */}
      {!isSidebarOpen && (
        <button
          ref={menuButtonRef}
          className="absolute top-4 left-4 z-10 bg-[#326789] text-white p-2 rounded-md sm:hidden"
          onClick={(e) => {
            e.stopPropagation();
            setSidebarOpen(true);
          }}
        >
          ☰
        </button>
      )}

      {/* Sidebar */}
      <aside
        ref={sidebarRef}
        className={`fixed top-0 left-0 h-full w-64 bg-[#326789] text-white p-4 pt-4 transition-transform transform ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } md:w-64 sm:w-64 lg:w-64`}
      >
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-2xl font-bold">Admin Panel</h2>
          <button
            className="bg-[#E9EEF2] text-[#326789] p-1 rounded-md"
            onClick={() => setSidebarOpen(false)}
          >
            ❌
          </button>
        </div>

        <nav>
          <ul className="space-y-1">
            {['users', 'admins', 'reports', 'links', 'settings'].map((tab) => (
              <li key={tab}>
                <button
                  className="w-full text-left px-4 py-2 rounded-md hover:bg-[#78A6C8] flex items-center"
                  onClick={() => {
                    setActiveTab(tab);
                    setSidebarOpen(false);
                  }}
                >
                  <i className={`fas fa-${tab === 'users' ? 'users' : tab === 'admins' ? 'user-shield' : tab === 'reports' ? 'file-alt' : tab === 'links' ? 'link' : 'cog'} mr-2`}></i>
                  {tab.charAt(0).toUpperCase() + tab.slice(1)}
                </button>
              </li>
            ))}
          </ul>
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
      <header className="bg-white shadow p-4 w-full flex justify-between items-center sm:pl-4">
        {/* Admin Dashboard title */}
        <h1 className="text-2xl font-bold pl-10 sm:pl-0">Admin Dashboard</h1>
        
        {/* Tab Buttons - only visible on larger screens, positioned on the far right */}
        <div className="hidden sm:flex space-x-4">
          {['users', 'admins', 'reports', 'links', 'settings'].map((tab) => (
            <button
              key={tab}
              className={`px-4 py-2 rounded-md flex items-center ${
                activeTab === tab ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
              }`}
              onClick={() => setActiveTab(tab)}
            >
              <i className={`fas fa-${tab === 'users' ? 'users' : tab === 'admins' ? 'user-shield' : tab === 'reports' ? 'file-alt' : tab === 'links' ? 'link' : 'cog'} mr-2`}></i>
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
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

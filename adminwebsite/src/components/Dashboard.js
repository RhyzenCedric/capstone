// src/Dashboard.js
import React, { useState } from 'react';
import Users from './Users';
import Admins from './Admins';
import Reports from './Reports';
import Links from './Links';
import Settings from './Settings';

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('users');

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>

      {/* Tab Navigation */}
      <div className="flex space-x-4 mb-6">
        <button
          className={`px-4 py-2 rounded-md ${
            activeTab === 'users' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
          }`}
          onClick={() => setActiveTab('users')}
        >
          Users
        </button>
        <button
          className={`px-4 py-2 rounded-md ${
            activeTab === 'admins' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
          }`}
          onClick={() => setActiveTab('admins')}
        >
          Admins
        </button>
        <button
          className={`px-4 py-2 rounded-md ${
            activeTab === 'reports' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
          }`}
          onClick={() => setActiveTab('reports')}
        >
          Reports
        </button>
        <button
          className={`px-4 py-2 rounded-md ${
            activeTab === 'links' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
          }`}
          onClick={() => setActiveTab('links')}
        >
          Links
        </button>
        <button
          className={`px-4 py-2 rounded-md ${
            activeTab === 'settings' ? 'bg-[#326789] text-white' : 'bg-white text-[#326789]'
          }`}
          onClick={() => setActiveTab('settings')}
        >
          Settings
        </button>
      </div>

      {/* Tab Content */}
      <div className="p-4 bg-[#E9EEF2] rounded-md">
        {activeTab === 'users' && <Users />}
        {activeTab === 'admins' && <Admins />}
        {activeTab === 'reports' && <Reports />}
        {activeTab === 'links' && <Links />}
        {activeTab === 'settings' && <Settings />}
      </div>
    </div>
  );
};

export default Dashboard;

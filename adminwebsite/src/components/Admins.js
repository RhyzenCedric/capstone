// src/Admins.js
import React, { useState } from 'react';

const initialAdmins = [
  { id: 1, firstName: 'Admin', lastName: 'One', username: 'admin1' },
  { id: 2, firstName: 'Admin', lastName: 'Two', username: 'admin2' },
];

const Admins = () => {
  const [admins, setAdmins] = useState(initialAdmins);

  const handleDeleteAdmin = (id) => {
    setAdmins(admins.filter((admin) => admin.id !== id));
  };

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Admins</h2>
      <button className="mb-4 bg-[#326789] text-white px-4 py-2 rounded-md">
        Add Admin
      </button>
      <table className="min-w-full bg-white">
        <thead>
          <tr className="bg-[#78A6C8] text-white">
            <th className="py-2 px-4">Full Name</th>
            <th className="py-2 px-4">Username</th>
            <th className="py-2 px-4">Actions</th>
          </tr>
        </thead>
        <tbody>
          {admins.map((admin) => (
            <tr key={admin.id} className="border-b">
              <td className="py-2 px-4">{`${admin.firstName} ${admin.lastName}`}</td>
              <td className="py-2 px-4">{admin.username}</td>
              <td className="py-2 px-4">
                <button className="text-blue-500">Edit</button>
                <button
                  className="text-red-500 ml-2"
                  onClick={() => handleDeleteAdmin(admin.id)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Admins;

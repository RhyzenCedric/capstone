// src/Users.js
import React, { useState } from 'react';

const initialUsers = [
  { id: 1, firstName: 'John', lastName: 'Doe', username: 'johndoe' },
  { id: 2, firstName: 'Jane', lastName: 'Smith', username: 'janesmith' },
];

const Users = () => {
  const [users, setUsers] = useState(initialUsers);

  const handleDeleteUser = (id) => {
    setUsers(users.filter((user) => user.id !== id));
  };

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Users</h2>
      <button className="mb-4 bg-[#326789] text-white px-4 py-2 rounded-md">
        Add User
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
          {users.map((user) => (
            <tr key={user.id} className="border-b">
              <td className="py-2 px-4">{`${user.firstName} ${user.lastName}`}</td>
              <td className="py-2 px-4">{user.username}</td>
              <td className="py-2 px-4">
                <button className="text-blue-500">Edit</button>
                <button
                  className="text-red-500 ml-2"
                  onClick={() => handleDeleteUser(user.id)}
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

export default Users;

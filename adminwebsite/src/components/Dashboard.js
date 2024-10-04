// src/Dashboard.js
import React, { useState } from 'react';

const initialData = [];

const Dashboard = () => {
  const [data, setData] = useState(initialData);
  const [inputValue, setInputValue] = useState('');

  const handleAdd = () => {
    if (inputValue) {
      setData([...data, inputValue]);
      setInputValue('');
    }
  };

  const handleDelete = (index) => {
    const newData = data.filter((_, i) => i !== index);
    setData(newData);
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>

      <div className="flex mb-4">
        <input
          type="text"
          className="border p-2 flex-grow"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Add new item"
        />
        <button
          className="bg-blue-500 text-white px-4 ml-2"
          onClick={handleAdd}
        >
          Add
        </button>
      </div>

      <ul className="list-disc pl-5">
        {data.map((item, index) => (
          <li key={index} className="flex justify-between items-center mb-2">
            <span>{item}</span>
            <button
              className="bg-red-500 text-white px-2"
              onClick={() => handleDelete(index)}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Dashboard;

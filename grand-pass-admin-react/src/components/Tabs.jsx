import React from 'react'

export default function Tabs({ activeTab, setActiveTab }) {
  const tabs = ['juegos', 'partidas', 'usuarios']
  return (
    <div className="bg-white shadow p-2 flex space-x-2">
      {tabs.map(tab => (
        <button
          key={tab}
          onClick={() => setActiveTab(tab)}
          className={`px-4 py-2 rounded ${activeTab === tab ? 'bg-blue-600 text-white' : 'bg-gray-100'}`}
        >
          {tab.charAt(0).toUpperCase() + tab.slice(1)}
        </button>
      ))}
    </div>
  )
}
import React, { useState } from 'react'
import Tabs from './components/Tabs'
import Juegos from './pages/Juegos'
import Partidas from './pages/Partidas'
import Usuarios from './pages/Usuarios'

export default function App() {
  const [activeTab, setActiveTab] = useState('juegos')

  const renderContent = () => {
    switch (activeTab) {
      case 'juegos': return <Juegos />
      case 'partidas': return <Partidas />
      case 'usuarios': return <Usuarios />
      default: return null
    }
  }

  return (
    <div className="p-4 bg-gray-100 min-h-screen">
      <header className="bg-blue-600 text-white p-4 rounded-t-lg text-2xl font-bold shadow-md">Grand Pass</header>
      <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />
      {renderContent()}
    </div>
  )
}
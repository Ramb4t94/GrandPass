import React, { useEffect, useState } from 'react';
import { collection, onSnapshot } from 'firebase/firestore';
import { db } from '../firebase';

export default function Partidas() {
  const [matchmaking, setMatchmaking] = useState([]);

  useEffect(() => {
    const unsubscribe = onSnapshot(collection(db, 'Matchmaking'), (snapshot) => {
      const datos = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setMatchmaking(datos);
    });

    return () => unsubscribe();
  }, []);

  return (
    <div className="p-4 space-y-4">
      <div className="text-center text-xl font-bold">
        Usuarios activos en matchmaking: {matchmaking.length}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
        {matchmaking.length === 0 ? (
          <p className="text-gray-500 col-span-full text-center">No hay usuarios en matchmaking actualmente.</p>
        ) : (
          matchmaking.map((entry) => (
            <div key={entry.id} className="bg-white shadow-md rounded-2xl p-4">
              <h3 className="text-lg font-semibold mb-2">🎮 Juego: {entry.gameId}</h3>
              <p><strong>👤 Usuario:</strong> {entry.userName}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

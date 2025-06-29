import React, { useEffect, useState } from 'react';
import { collection, getDocs, doc, getDoc, setDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../firebase';

const coleccionesDisponibles = ['TablerosJuegos', 'NaipesJuegos', 'OtrosJuegos'];

const capitalizar = (texto) =>
  texto.trim().charAt(0).toUpperCase() + texto.trim().slice(1).toLowerCase();

const formatearTextoLargo = (texto) => {
  const limpio = texto.trim();
  if (!limpio.endsWith('.')) return capitalizar(limpio) + '.';
  return capitalizar(limpio);
};

const limpiarNumero = (valor) => {
  let limpio = parseInt(valor, 10);
  if (isNaN(limpio) || limpio < 2) return null;
  return limpio.toString();
};

export default function Juegos() {
  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState('');
  const [juegos, setJuegos] = useState([]);
  const [juegoSeleccionado, setJuegoSeleccionado] = useState(null);
  const [modoEdicion, setModoEdicion] = useState(false);
  const [errorNumJugadores, setErrorNumJugadores] = useState('');

  useEffect(() => {
    if (!categoriaSeleccionada) return;
    const fetchJuegos = async () => {
      const snapshot = await getDocs(collection(db, categoriaSeleccionada));
      const lista = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setJuegos(lista);
      setJuegoSeleccionado(null);
    };
    fetchJuegos();
  }, [categoriaSeleccionada]);

  const seleccionarJuego = async (id) => {
    const docRef = doc(db, categoriaSeleccionada, id);
    const docSnap = await getDoc(docRef);
    if (docSnap.exists()) {
      setJuegoSeleccionado({ id, ...docSnap.data() });
      setModoEdicion(false);
    }
  };

  const handleCampo = (campo, valor) => {
    setJuegoSeleccionado({ ...juegoSeleccionado, [campo]: valor });

    if (campo === 'num_jugadores') {
      if (!/^\d*$/.test(valor)) return;
      const numero = parseInt(valor, 10);
      if (isNaN(numero) || numero < 2) {
        setErrorNumJugadores('Debe ser un número entero mayor o igual a 2.');
      } else {
        setErrorNumJugadores('');
      }
    }
  };

  const guardarJuego = async () => {
    if (!juegoSeleccionado?.id || !categoriaSeleccionada)
      return alert("Faltan datos.");

    const { id, name, description, rules, num_jugadores } = juegoSeleccionado;

    if (!name.trim() || !description.trim() || !rules.trim()) {
      return alert("Todos los campos deben estar completos.");
    }

    const jugadoresValidados = limpiarNumero(num_jugadores);
    if (!jugadoresValidados) {
      setErrorNumJugadores('Debe ser un número entero mayor o igual a 2.');
      return alert('Error en número de jugadores.');
    }

    const juegoFormateado = {
      id,
      name: capitalizar(name),
      description: formatearTextoLargo(description),
      rules: formatearTextoLargo(rules),
      num_jugadores: jugadoresValidados,
    };

    const docRef = doc(db, categoriaSeleccionada, id);
    await setDoc(docRef, juegoFormateado);

    setJuegos((prev) => {
      const index = prev.findIndex(j => j.id === id);
      if (index !== -1) {
        const actualizada = [...prev];
        actualizada[index] = juegoFormateado;
        return actualizada;
      } else {
        return [...prev, juegoFormateado];
      }
    });

    setJuegoSeleccionado(juegoFormateado);
    alert('Juego guardado con éxito.');
    setModoEdicion(false);
  };

  const eliminarJuego = async () => {
    if (!juegoSeleccionado?.id || !categoriaSeleccionada) return;
    const confirm = window.confirm("¿Estás seguro que deseas eliminar este juego?");
    if (!confirm) return;

    await deleteDoc(doc(db, categoriaSeleccionada, juegoSeleccionado.id));
    setJuegos(prev => prev.filter(j => j.id !== juegoSeleccionado.id));
    setJuegoSeleccionado(null);
    alert('Juego eliminado.');
  };

  const añadirJuego = () => {
    const nuevoId = `juego${Date.now()}`;
    const nuevoJuego = {
      id: nuevoId,
      name: '',
      description: '',
      rules: '',
      num_jugadores: '2',// antes era 1
    };
    setJuegoSeleccionado(nuevoJuego);
    setModoEdicion(true);
    setJuegos(prev => [...prev, nuevoJuego]);
  };

  return (
    <div className="p-4 space-y-4">
      <select
        className="p-2 border rounded w-full"
        value={categoriaSeleccionada}
        onChange={(e) => setCategoriaSeleccionada(e.target.value)}
      >
        <option value="">Selecciona una categoría</option>
        {coleccionesDisponibles.map(cat => (
          <option key={cat} value={cat}>{cat}</option>
        ))}
      </select>

      {categoriaSeleccionada && (
        <select
          className="p-2 border rounded w-full"
          value={juegoSeleccionado?.id || ''}
          onChange={(e) => seleccionarJuego(e.target.value)}
        >
          <option value="">Selecciona un juego</option>
          {juegos.map(j => (
            <option key={j.id} value={j.id}>{j.name}</option>
          ))}
        </select>
      )}

      {juegoSeleccionado && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white shadow-md rounded-2xl p-4">
              <h3 className="text-lg font-semibold mb-2">Reglas</h3>
              {modoEdicion ? (
                <textarea
                  className="w-full border p-2 rounded"
                  value={juegoSeleccionado.rules}
                  onChange={(e) => handleCampo('rules', e.target.value)}
                />
              ) : (
                <p>{juegoSeleccionado.rules || 'Sin reglas'}</p>
              )}
            </div>
            <div className="bg-white shadow-md rounded-2xl p-4">
              <h3 className="text-lg font-semibold mb-2">Descripción</h3>
              {modoEdicion ? (
                <textarea
                  className="w-full border p-2 rounded"
                  value={juegoSeleccionado.description}
                  onChange={(e) => handleCampo('description', e.target.value)}
                />
              ) : (
                <p>{juegoSeleccionado.description}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div className="bg-white shadow-md rounded-2xl p-4">
              <h3 className="text-lg font-semibold mb-2">Nombre</h3>
              {modoEdicion ? (
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={juegoSeleccionado.name}
                  onChange={(e) => handleCampo('name', e.target.value)}
                />
              ) : (
                <p>{juegoSeleccionado.name}</p>
              )}
            </div>
            <div className="bg-white shadow-md rounded-2xl p-4">
              <h3 className="text-lg font-semibold mb-2">N° Jugadores</h3>
              {modoEdicion ? (
                <>
                  <input
                    type="text"
                    className="w-full border p-2 rounded"
                    value={juegoSeleccionado.num_jugadores}
                    onChange={(e) => handleCampo('num_jugadores', e.target.value.replace(/[^\d]/g, ''))}
                  />
                  {errorNumJugadores && (
                    <p className="text-red-500 text-sm mt-1">{errorNumJugadores}</p>
                  )}
                </>
              ) : (
                <p>{juegoSeleccionado.num_jugadores}</p>
              )}
            </div>
          </div>
        </>
      )}

      {categoriaSeleccionada && (
        <div className="flex justify-center space-x-4 mt-4">
          <button
            onClick={() => setModoEdicion(true)}
            className="bg-blue-500 hover:bg-blue-600 text-white font-semibold px-4 py-2 rounded-xl"
          >
            Modificar
          </button>
          <button
            onClick={añadirJuego}
            className="bg-green-500 hover:bg-green-600 text-white font-semibold px-4 py-2 rounded-xl"
          >
            Añadir
          </button>
          <button
            onClick={eliminarJuego}
            className="bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-2 rounded-xl"
          >
            Eliminar
          </button>
          <button
            onClick={guardarJuego}
            className="bg-yellow-500 hover:bg-yellow-600 text-white font-semibold px-4 py-2 rounded-xl"
          >
            Guardar
          </button>
        </div>
      )}
    </div>
  );
}

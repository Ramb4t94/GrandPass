import React, { useEffect, useState } from 'react';
import { collection, getDocs, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { db } from '../firebase';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [usuariosFiltrados, setUsuariosFiltrados] = useState([]);
  const [editandoId, setEditandoId] = useState(null);
  const [usuarioEditado, setUsuarioEditado] = useState({});
  const [busqueda, setBusqueda] = useState('');

  // FUNCIONES DE VALIDACIÓN
  const esTextoValido = (texto) => /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(texto.trim());
  const esFechaValida = (fecha) => /^(0[1-9]|[12]\d|3[01])\/(0[1-9]|1[0-2])\/\d{4}$/.test(fecha.trim());

  const capitalizarCorrectamente = (texto) => {
    return texto
      .toLowerCase()
      .replace(/(^|\s)([a-záéíóúñ])/g, (_, espacio, letra) => espacio + letra.toUpperCase());
  };

  // CARGA DE USUARIOS
  const fetchUsuarios = async () => {
    const snapshot = await getDocs(collection(db, 'Usuarios'));
    const listaUsuarios = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
    }));
    setUsuarios(listaUsuarios);
    setUsuariosFiltrados(listaUsuarios);
  };

  useEffect(() => {
    fetchUsuarios();
  }, []);

  useEffect(() => {
    const filtro = busqueda.toLowerCase();
    const filtrados = usuarios.filter(u =>
      (u.name?.toLowerCase().includes(filtro) ||
       u.surname1?.toLowerCase().includes(filtro) ||
       u.surname2?.toLowerCase().includes(filtro) ||
       u.phone?.toLowerCase().includes(filtro))
    );
    setUsuariosFiltrados(filtrados);
  }, [busqueda, usuarios]);

  // CAMBIOS EN CAMPOS
  const manejarCambio = (campo, valor) => {
    setUsuarioEditado({ ...usuarioEditado, [campo]: valor });
  };

  const comenzarEdicion = (usuario) => {
    setEditandoId(usuario.id);
    setUsuarioEditado(usuario);
  };

  const cancelarEdicion = () => {
    setEditandoId(null);
    setUsuarioEditado({});
  };

  // GUARDAR CAMBIOS
  const guardarCambios = async () => {
    let { name, surname1, surname2, birth, phone } = usuarioEditado;

    // Teléfono: añadir +56 si falta
    if (!phone.startsWith('+56')) {
      phone = '+56' + phone.replace(/[^0-9]/g, '');
    }

    const soloNumeros = phone.slice(3);
    if (!esTextoValido(name) || !esTextoValido(surname1) || !esTextoValido(surname2)) {
      alert('Nombre y apellidos no deben contener números ni símbolos.');
      return;
    }

    if (!esFechaValida(birth)) {
      alert('La fecha de nacimiento debe estar en formato DD/MM/AAAA');
      return;
    }

    if (!/^\d{9}$/.test(soloNumeros)) {
      alert('El número de teléfono debe contener exactamente 9 dígitos después de +56.');
      return;
    }

    // Capitalizar nombres
    name = capitalizarCorrectamente(name);
    surname1 = capitalizarCorrectamente(surname1);
    surname2 = capitalizarCorrectamente(surname2);

    const docRef = doc(db, 'Usuarios', editandoId);
    const { id, uid, createdAt, ...rest } = usuarioEditado;

    await updateDoc(docRef, {
      ...rest,
      name,
      surname1,
      surname2,
      birth,
      phone
    });

    alert('Usuario actualizado correctamente');
    setEditandoId(null);
    setUsuarioEditado({});
    fetchUsuarios();
  };

  const eliminarUsuario = async (id) => {
    if (!window.confirm("¿Estás seguro de eliminar este usuario?")) return;
    await deleteDoc(doc(db, 'Usuarios', id));
    alert('Usuario eliminado.');
    fetchUsuarios();
  };

  return (
    <div className="p-4 space-y-4">
      <div className="flex justify-between items-center">
        <input
          type="text"
          placeholder="Buscar por nombre, apellido o teléfono"
          className="w-full p-2 border rounded"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
        <div className="ml-4 text-sm text-gray-600">
          Total: {usuariosFiltrados.length} usuario(s)
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {usuariosFiltrados.map((usuario) => (
          <div key={usuario.id} className="bg-white shadow-md rounded-2xl p-4 space-y-2">
            {editandoId === usuario.id ? (
              <>
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={usuarioEditado.name}
                  onChange={(e) => manejarCambio('name', e.target.value)}
                  placeholder="Nombre"
                />
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={usuarioEditado.surname1}
                  onChange={(e) => manejarCambio('surname1', e.target.value)}
                  placeholder="Apellido Paterno"
                />
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={usuarioEditado.surname2}
                  onChange={(e) => manejarCambio('surname2', e.target.value)}
                  placeholder="Apellido Materno"
                />
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={usuarioEditado.birth}
                  onChange={(e) => manejarCambio('birth', e.target.value)}
                  placeholder="Fecha de Nacimiento (DD/MM/AAAA)"
                />
                <input
                  type="text"
                  className="w-full border p-2 rounded"
                  value={usuarioEditado.phone}
                  onChange={(e) => manejarCambio('phone', e.target.value)}
                  placeholder="Teléfono (+56XXXXXXXXX)"
                />
                <div className="flex gap-2 mt-2">
                  <button onClick={guardarCambios} className="bg-yellow-500 hover:bg-yellow-600 text-white px-4 py-1 rounded-xl">Guardar</button>
                  <button onClick={cancelarEdicion} className="bg-gray-400 hover:bg-gray-500 text-white px-4 py-1 rounded-xl">Cancelar</button>
                </div>
              </>
            ) : (
              <>
                <h3 className="text-lg font-semibold">{usuario.name} {usuario.surname1} {usuario.surname2}</h3>
                <p><strong>Nacimiento:</strong> {usuario.birth}</p>
                <p><strong>Teléfono:</strong> {usuario.phone}</p>
                <div className="flex gap-2 mt-2">
                  <button onClick={() => comenzarEdicion(usuario)} className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-1 rounded-xl">Editar</button>
                  <button onClick={() => eliminarUsuario(usuario.id)} className="bg-red-500 hover:bg-red-600 text-white px-4 py-1 rounded-xl">Eliminar</button>
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

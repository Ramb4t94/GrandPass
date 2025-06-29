// Configura tus credenciales aquí
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";


const firebaseConfig = {
  apiKey: "AIzaSyB98RE62_gicSZIUwBG-hjCZb0OtPUxSLM",

  authDomain: "grandpassdatabase.firebaseapp.com",

  projectId: "grandpassdatabase",

  storageBucket: "grandpassdatabase.firebasestorage.app",

  messagingSenderId: "656494308685",

  appId: "1:656494308685:web:8a6d48b9e440f5e1d55b04",


};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
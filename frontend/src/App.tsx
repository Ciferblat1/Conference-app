
import { Routes, Route } from 'react-router';

import './App.css'

import DirectionsPage from './pages/DirectionsPage'
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';

function App() {

  return (
    <Routes>
      <Route path="/" element={<DirectionsPage/>}/>
      <Route path="/register" element={<RegisterPage/>}/>
      <Route path="/login" element={<LoginPage/>}/>
    </Routes>
  )
}

export default App

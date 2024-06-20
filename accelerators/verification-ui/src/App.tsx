import React from 'react';
import logo from './assets/logo.svg';
import './App.css';
import Home from "./pages/Home";
import Offline from "./pages/Offline";
import {RouterProvider, createBrowserRouter} from "react-router-dom";

const router = createBrowserRouter([
    {
        path: '/',
        element: <Home/>
    },
    {
        path: '/offline',
        element: <Offline/>
    }
])

function App() {
    return (
        <RouterProvider router={router}/>
    );
}

export default App;

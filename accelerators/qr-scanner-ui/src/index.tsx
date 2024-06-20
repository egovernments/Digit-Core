import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {GlobalStyles, ThemeProvider} from "@mui/material";
import {InjiTheme} from "./utils/inji-theme";

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

// Store in a constant to avoid re-rendering and recalculating the style
const inputGlobalStyles = <GlobalStyles styles={{ fontFamily: 'Inter'}} />;

root.render(
  <React.StrictMode>
    <ThemeProvider theme={InjiTheme}>
        {inputGlobalStyles}
        <App />
    </ThemeProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

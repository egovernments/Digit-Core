import React from "react";
import { LocalMovies } from "./LocalMovies";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalMovies",
  component: LocalMovies,
};

export const Default = () => <LocalMovies />;
export const Fill = () => <LocalMovies fill="blue" />;
export const Size = () => <LocalMovies height="50" width="50" />;
export const CustomStyle = () => <LocalMovies style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalMovies className="custom-class" />;

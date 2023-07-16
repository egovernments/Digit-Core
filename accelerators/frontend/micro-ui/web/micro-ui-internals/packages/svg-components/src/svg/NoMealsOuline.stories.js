import React from "react";
import { NoMealsOuline } from "./NoMealsOuline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NoMealsOuline",
  component: NoMealsOuline,
};

export const Default = () => <NoMealsOuline />;
export const Fill = () => <NoMealsOuline fill="blue" />;
export const Size = () => <NoMealsOuline height="50" width="50" />;
export const CustomStyle = () => <NoMealsOuline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoMealsOuline className="custom-class" />;

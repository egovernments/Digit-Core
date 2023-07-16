import React from "react";
import { PeopleOutline } from "./PeopleOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PeopleOutline",
  component: PeopleOutline,
};

export const Default = () => <PeopleOutline />;
export const Fill = () => <PeopleOutline fill="blue" />;
export const Size = () => <PeopleOutline height="50" width="50" />;
export const CustomStyle = () => <PeopleOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PeopleOutline className="custom-class" />;

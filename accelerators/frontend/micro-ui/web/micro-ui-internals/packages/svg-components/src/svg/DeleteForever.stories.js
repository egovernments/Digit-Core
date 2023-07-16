import React from "react";
import { DeleteForever } from "./DeleteForever";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DeleteForever",
  component: DeleteForever,
};

export const Default = () => <DeleteForever />;
export const Fill = () => <DeleteForever fill="blue" />;
export const Size = () => <DeleteForever height="50" width="50" />;
export const CustomStyle = () => <DeleteForever style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DeleteForever className="custom-class" />;

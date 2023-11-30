import React from "react";
import { FavoriteBorder } from "./FavoriteBorder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FavoriteBorder",
  component: FavoriteBorder,
};

export const Default = () => <FavoriteBorder />;
export const Fill = () => <FavoriteBorder fill="blue" />;
export const Size = () => <FavoriteBorder height="50" width="50" />;
export const CustomStyle = () => <FavoriteBorder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FavoriteBorder className="custom-class" />;

export const Clickable = () => <FavoriteBorder onClick={()=>console.log("clicked")} />;

const Template = (args) => <FavoriteBorder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

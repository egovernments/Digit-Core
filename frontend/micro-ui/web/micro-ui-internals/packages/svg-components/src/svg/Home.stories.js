import React from "react";
import { Home } from "./Home";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Home",
  component: Home,
};

export const Default = () => <Home />;
export const Fill = () => <Home fill="blue" />;
export const Size = () => <Home height="50" width="50" />;
export const CustomStyle = () => <Home style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Home className="custom-class" />;

export const Clickable = () => <Home onClick={()=>console.log("clicked")} />;

const Template = (args) => <Home {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

import React from "react";
import { LocalPolice } from "./LocalPolice";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPolice",
  component: LocalPolice,
};

export const Default = () => <LocalPolice />;
export const Fill = () => <LocalPolice fill="blue" />;
export const Size = () => <LocalPolice height="50" width="50" />;
export const CustomStyle = () => <LocalPolice style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPolice className="custom-class" />;

export const Clickable = () => <LocalPolice onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalPolice {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

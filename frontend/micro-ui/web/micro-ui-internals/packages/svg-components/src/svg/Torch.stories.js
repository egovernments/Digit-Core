import React from "react";
import { Torch } from "./Torch";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Torch",
  component: Torch,
};

export const Default = () => <Torch />;
export const Fill = () => <Torch fill="blue" />;
export const Size = () => <Torch height="50" width="50" />;
export const CustomStyle = () => <Torch style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Torch className="custom-class" />;
export const Clickable = () => <Torch onClick={()=>console.log("clicked")} />;

const Template = (args) => <Torch {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};


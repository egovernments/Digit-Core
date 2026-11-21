import React from "react";
import { Dialpad } from "./Dialpad";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Dialpad",
  component: Dialpad,
};

export const Default = () => <Dialpad />;
export const Fill = () => <Dialpad fill="blue" />;
export const Size = () => <Dialpad height="50" width="50" />;
export const CustomStyle = () => <Dialpad style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Dialpad className="custom-class" />;

export const Clickable = () => <Dialpad onClick={()=>console.log("clicked")} />;

const Template = (args) => <Dialpad {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

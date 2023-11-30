import React from "react";
import { Pets } from "./Pets";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Pets",
  component: Pets,
};

export const Default = () => <Pets />;
export const Fill = () => <Pets fill="blue" />;
export const Size = () => <Pets height="50" width="50" />;
export const CustomStyle = () => <Pets style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Pets className="custom-class" />;

export const Clickable = () => <Pets onClick={()=>console.log("clicked")} />;

const Template = (args) => <Pets {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

import React from "react";
import { Attractions } from "./Attractions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Attractions",
  component: Attractions,
};

export const Default = () => <Attractions />;
export const Fill = () => <Attractions fill="blue" />;
export const Size = () => <Attractions height="50" width="50" />;
export const CustomStyle = () => <Attractions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Attractions className="custom-class" />;
export const Clickable = () => <Attractions onClick={()=>console.log("clicked")} />;

const Template = (args) => <Attractions {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

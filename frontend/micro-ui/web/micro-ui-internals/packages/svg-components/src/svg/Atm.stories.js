import React from "react";
import { Atm } from "./Atm";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Atm",
  component: Atm,
};

export const Default = () => <Atm />;
export const Fill = () => <Atm fill="blue" />;
export const Size = () => <Atm height="50" width="50" />;
export const CustomStyle = () => <Atm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Atm className="custom-class" />;
export const Clickable = () => <Atm onClick={()=>console.log("clicked")} />;

const Template = (args) => <Atm {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

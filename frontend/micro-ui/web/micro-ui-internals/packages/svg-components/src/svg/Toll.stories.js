import React from "react";
import { Toll } from "./Toll";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Toll",
  component: Toll,
};

export const Default = () => <Toll />;
export const Fill = () => <Toll fill="blue" />;
export const Size = () => <Toll height="50" width="50" />;
export const CustomStyle = () => <Toll style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Toll className="custom-class" />;
export const Clickable = () => <Toll onClick={()=>console.log("clicked")} />;

const Template = (args) => <Toll {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

import React from "react";
import { Theaters } from "./Theaters";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Theaters",
  component: Theaters,
};

export const Default = () => <Theaters />;
export const Fill = () => <Theaters fill="blue" />;
export const Size = () => <Theaters height="50" width="50" />;
export const CustomStyle = () => <Theaters style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Theaters className="custom-class" />;
export const Clickable = () => <Theaters onClick={()=>console.log("clicked")} />;

const Template = (args) => <Theaters {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

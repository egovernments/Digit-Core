import React from "react";
import { North } from "./North";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "North",
  component: North,
};

export const Default = () => <North />;
export const Fill = () => <North fill="blue" />;
export const Size = () => <North height="50" width="50" />;
export const CustomStyle = () => <North style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <North className="custom-class" />;

export const Clickable = () => <North onClick={()=>console.log("clicked")} />;

const Template = (args) => <North {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

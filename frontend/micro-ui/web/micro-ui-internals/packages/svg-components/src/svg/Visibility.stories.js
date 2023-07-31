import React from "react";
import { Visibility } from "./Visibility";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Visibility",
  component: Visibility,
};

export const Default = () => <Visibility />;
export const Fill = () => <Visibility fill="blue" />;
export const Size = () => <Visibility height="50" width="50" />;
export const CustomStyle = () => <Visibility style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Visibility className="custom-class" />;
export const Clickable = () => <Visibility onClick={()=>console.log("clicked")} />;

const Template = (args) => <Visibility {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

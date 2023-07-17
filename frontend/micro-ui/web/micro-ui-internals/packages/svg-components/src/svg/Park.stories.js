import React from "react";
import { Park } from "./Park";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Park",
  component: Park,
};

export const Default = () => <Park />;
export const Fill = () => <Park fill="blue" />;
export const Size = () => <Park height="50" width="50" />;
export const CustomStyle = () => <Park style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Park className="custom-class" />;

export const Clickable = () => <Park onClick={()=>console.log("clicked")} />;

const Template = (args) => <Park {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

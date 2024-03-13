import React from "react";
import { AutoDelete } from "./AutoDelete";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AutoDelete",
  component: AutoDelete,
};

export const Default = () => <AutoDelete />;
export const Fill = () => <AutoDelete fill="blue" />;
export const Size = () => <AutoDelete height="50" width="50" />;
export const CustomStyle = () => <AutoDelete style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AutoDelete className="custom-class" />;
export const Clickable = () => <AutoDelete onClick={()=>console.log("clicked")} />;

const Template = (args) => <AutoDelete {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
